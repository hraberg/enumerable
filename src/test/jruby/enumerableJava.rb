require 'java'

import 'lambda.Fn1'
import 'lambda.enumerable.collection.EnumerableModule'
import 'lambda.jruby.LambdaJRuby'
import 'lambda.enumerable.EnumerableJRubyTest'
import 'org.jruby.javasupport.JavaEmbedUtils'
import 'org.jruby.Ruby'

module EnumerableJava
  def self.included(host)
    host.class_eval do
      alias :original_each :each
      alias :original_to_a :to_a
      instance_methods(false).select {|m| Enumerable.method_defined? m}.each {|m| remove_method m}

      def each &block
        EnumerableJRubyTest.debug "calling each"
        to_java.each(to_fn block).delegate
      end
    end
  end

  private
  def to_java
    EnumerableModule.extend(Array == self.class || Hash == self.class ? self : original_to_a)
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

module Enumerable
  @@original_instance_methods = instance_methods
  instance_methods.each {|m| remove_method m unless m =~ /^enum/}

  def respond_to?(method)
    @@original_instance_methods.member?(method.to_s) || super
  end

  def to_a
    EnumerableJRubyTest.debug "calling to_a"
    to_java.to_list.to_a
  end

  def method_missing(name, *args, &block)
    EnumerableJRubyTest.debug "calling #{name} with #{args} #{block}"
    args = args.push(to_fn block) if block_given?
    args.collect! {|a| a.class == Proc ? to_fn(a) : a}

    name = name.to_s.sub "?", ""

    begin
      result = to_java.send name, *args
      unnest_java_collections result
    rescue NameError, Java::JavaLang::IllegalArgumentException => e
      EnumerableJRubyTest.debug e.to_s
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
      h.each {|e| h[e.key] = unnest_java_collections e.value}
    elsif o == Java::LambdaEnumerableCollection::EList
      Array
    elsif o == Java::LambdaEnumerableCollection::EMap
      Hash
    else
      o
    end
  end
end

