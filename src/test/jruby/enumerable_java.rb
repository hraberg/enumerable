require 'java'

import 'lambda.Fn1'
import 'lambda.enumerable.collection.EnumerableModule'
import 'lambda.jruby.LambdaJRuby'
import 'lambda.enumerable.jruby.JRubyTestBase'

# Redefines Enumerable in JRuby to use Enumerable.java.
# It 'almost' works as long as there's not too much duck typing going on.
#
# This version is quite slack, uses method_missing and is primarily concerned with making
# calls go through between Ruby and Enumerable.java, with limited concern on what happens
# once it does. For a more strict version, see 'enumerable_java_rubyspec.rb'.
#
# require 'enumerable_java' will include EnumerableJava in anyone who includes Enumerable.
# It also patches Array, Hash and Range when first loaded.
#
# This patch is mainly for running tests written in Ruby against Enumerable.java.

JRubyTestBase.debug "Loading Enumerable.java MonkeyPatch"

module Enumerable
  def self.included(host)
    host.class_eval do
      JRubyTestBase.debug "included Enumerable in #{host}"
      include EnumerableJava unless host.include? EnumerableJava
    end
  end

  JRubyTestBase.debug "undefining the Enumerable methods (except /^enum/)"
  @@original_instance_methods = instance_methods
  instance_methods.each {|m| undef_method m unless m =~ /^enum/}
  
  def self.original_method?(method)
    # Cannot use include? here as this method will be used by EnumerableJava before the dust has settled
    @@original_instance_methods.each{|m| return true if m == method}
    false
  end
  
  def respond_to?(method)
    @@original_instance_methods.include?(method.to_s) || super
  end
  
  def method_missing(name, *args, &block)
    java_object = to_java
    JRubyTestBase.debug "calling #{name} with #{args.inspect} #{block_given? ? block : "<no block>"} on #{self.class} as #{java_object.class}"

    args = args << to_fn(block) if block_given?
    args.collect! {|a| a.is_a?(Proc) ? to_fn(a) : a}

    java_name = name.to_s.sub "?", ""

    begin
      result = java_object.send java_name, *args
    rescue ArgumentError => e
      raise e if [:include?, :member?].include? name

      JRubyTestBase.debug "caught ArgumentError, trying again with implicit block"
      begin
        result = java_object.send java_name, Fn1.identity
      rescue
        raise e
      end
    rescue NoMethodError, TypeError
      raise
    rescue Java::JavaLang::NullPointerException => e
      JRubyTestBase.debug "caught NullPointerException, will rereaise as NoMethodError"
      raise NoMethodError, e.to_s
    rescue NameError, Java::JavaLang::ClassCastException, Java::JavaLang::IllegalArgumentException => e
      raise LocalJumpError if [:inject].include?(name) && !block_given?

      JRubyTestBase.debug "caught #{e.class} \"#{e.to_s}\", will reraise as ArgumentError"
      raise ArgumentError, e.to_s
    end
    return self if java_object.equal? result
    unnest_java_collections result
  end

  private
  def unnest_java_collections o
    if o.class.include? Java::JavaUtil::List
      o.to_a.collect! {|e| unnest_java_collections e}
    elsif o.class.include? Java::JavaUtil::Map
      h = {}
      h.put_all o
      h.each {|kv| h[kv[0]] = unnest_java_collections kv[1]}
    elsif o == Java::LambdaEnumerableCollection::EList
      Array
    elsif o == Java::LambdaEnumerableCollection::EMap
      Hash
    else
      o
    end
  end
end

module EnumerableJava
  def self.included(host)
    host.class_eval do
      JRubyTestBase.debug "included EnumerableJava in #{host}"
      JRubyTestBase.debug "undefining methods shadowing Enumerable on #{host}"
      instance_methods(false).select {|m| Enumerable.original_method? m}.each do |m|
        JRubyTestBase.debug "undefining #{m} on #{host}"
        undef_method m
      end
    end
  end

  def to_a
    JRubyTestBase.debug "calling to_a on #{self.class}"
    return self if is_a? Array
    to_java.to_list.to_a
  end
  
  private
  def internal_to_a
  	a = []
  	each {|o| a << o }
  	a
  end

  def to_java
    EnumerableModule.extend(Array == self.class || Hash == self.class ? self : internal_to_a)
  end

  def to_fn proc
    return Fn1.identity if proc.nil?
    
    arity = proc.arity
    arity = arity.abs if arity < 0
  
    to_fnx = "to_fn#{arity}"
    if LambdaJRuby.respond_to? to_fnx
      LambdaJRuby.send to_fnx, proc
    else
      raise ArgumentError, "Cannot convert #{proc} into a lambda.Fn0", caller
    end
  end
end

class Range
  include EnumerableJava
end

class Hash
  include EnumerableJava
end

class Array
  include EnumerableJava
end

module java::util::List
  remove_method :sort if instance_methods.include? "sort"
end
