require 'java'

import 'lambda.Fn1'
import 'lambda.enumerable.collection.EnumerableModule'
import 'lambda.jruby.LambdaJRuby'
import 'lambda.enumerable.jruby.JRubyTestBase'

module Enumerable
  def self.included(host)
    host.class_eval do
      include EnumerableJava
      JRubyTestBase.debug "included EnumerableJava in #{host}"
    end
  end

  @@original_instance_methods = instance_methods
  instance_methods.each {|m| remove_method m unless m =~ /^enum/}
  
  def self.original_method?(method)
    @@original_instance_methods.each{|m| return true if m == method}
    false
  end
  
  def respond_to?(method)
    @@original_instance_methods.member?(method.to_s) || super
  end

  def method_missing(name, *args, &block)
    j = to_java
    JRubyTestBase.debug "calling #{name} with #{args} #{block} on #{self.class} as #{j.class}"

    args = args.push(to_fn block) if block_given?
    args.collect! {|a| a.class == Proc ? to_fn(a) : a}

    name = name.to_s.sub "?", ""

    begin
      result = j.send name, *args
      return self if j.equal? result
      unnest_java_collections result
    rescue ArgumentError
      result = j.send name, Fn1.identity
      return self if j.equal? result
      unnest_java_collections result
    rescue NoMethodError
      raise
    rescue NameError, Java::JavaLang::ClassCastException, Java::JavaLang::IllegalArgumentException => e
      JRubyTestBase.debug e.to_s
      raise ArgumentError, e.to_s
    end
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
  
  # defined in Kernel haven't found a good way to get rid of it
  def select &block
    find_all block
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
  remove_method :sort
end
