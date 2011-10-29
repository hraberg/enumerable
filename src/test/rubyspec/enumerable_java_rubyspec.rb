require 'java'

import 'java.lang.Iterable'

import 'org.enumerable.lambda.Fn1'
import 'org.enumerable.lambda.enumerable.collection.EnumerableModule'
import 'org.enumerable.lambda.enumerable.collection.EIterable'
import 'org.enumerable.lambda.enumerable.collection.EList'
import 'org.enumerable.lambda.enumerable.collection.EMap'
import 'org.enumerable.lambda.enumerable.jruby.JRubyTestBase'
import 'org.enumerable.lambda.enumerable.jruby.QueueIterator'

# Redefines Enumerable in JRuby to use Enumerable.java for running with RubySpec.
#
# This version is much stricter than the one in enumerable_java.rb, and goes to great lengths
# trying to assure Ruby that this is the normal Enumerable, even falling back to the real
# implementation in the case of 'grep'. All Enumerator handling is also managed here, as
# Enumerable.java doesn't support Enumerators.
#
# require 'enumerable_java_rubyspec' will include EnumerableJava  ainnyone who includes Enumerable.
# It also patches Array, Hash and Range when first loaded.
#
# This patch is mainly for running the RubySpec for Enumerable against Enumerable.java.
#
# There are ~40 guards added to the RubySpec for Enumerable to skip certain tests Enumerable.java
# doesn't handle. These are the only changes made to the specs.
#
# There's also a patch (not source level) to MSpec's EqualMatcher to use Object#equals() instead
# of Ruby identity equal? as the objects really are the same in these cases, except that they 
# have been converted back and forth between Java by JRuby instances.
#
# Note that MSpec itself is running with Enumerable patched as well. I assume that's in line with
# RubySpec which is also running in the environment it is actually testing.

def java_debug(msg)
  JRubyTestBase.debug msg
end

java_debug "Loading Enumerable.java MonkeyPatch, RubySpec version"

module Enumerable
  def self.included(host)
    host.class_eval do
      java_debug "included Enumerable in #{host}"
      unless include? Iterable
        java_debug "including Iterable in #{host}"
        include Iterable
        def iterator
          EnumerableIterator.new(self)
        end
      end
      include EnumerableJava unless host.include? EnumerableJava
    end
  end

  # This is an iterator using blocking queues implemented in Java. Works similar to a Generator.  
  class EnumerableIterator < QueueIterator
    def initialize(enum)
      super()
      @enum = enum
    end

    def iterate
      @enum.each {|e| java_debug "yielding #{e} using EnumerableIterator" ;enque e}
    end
  end

  @@original_instance_methods = instance_methods
  def self.original_method?(method)
    # Cannot use include? here as this method will be used by EnumerableJava before the dust has settled
    @@original_instance_methods.each{|m| return true if m == method}
    false
  end
  
  def all?(&block)
    java_debug "calling all? with #{block_given? ? block : "<no block>"} on #{self.class}"
    to_j.all(to_fn1 block)
  end
  
  def any?(&block)
    java_debug "calling any? with #{block_given? ? block : "<no block>"} on #{self.class}"
    to_j.any(to_fn1 block)
  end

  def collect(&block)
    java_debug "calling collect with #{block_given? ? block : "<no block>"} on #{self.class}"
    unnest_java_collections to_j.collect(to_fn1 block)
  end

  alias :map :collect

  def count(obj = NotSupplied, &block)
    java_debug "calling count with #{obj}, #{block_given? ? block : "<no block>"} on #{self.class}"
    if obj != NotSupplied
      STDERR.puts "warning: given block not used" if block_given?
      to_j.count obj
    elsif block_given?
      to_j.count to_fn1(block)
    else
      to_j.count
    end
  end  

  def cycle(times = nil, &block)
    begin
      java_debug "calling cycle with #{times}, #{block_given? ? block : "<no block>"} on #{self.class}"
      if times.nil?
        result = to_j.cycle to_fn1(block)
      else
        raise TypeError, "can't convert #{times.class} into Integer" unless times.respond_to? :to_int
        result = to_j.cycle times.to_int, to_fn1(block)
      end
      return nil if result.nil? || block_given?
      result.to_a.to_enum
    rescue NativeException => e
      raise e.cause if e.cause.is_a? Java::OrgJrubyExceptions::JumpException
      raise
    end
  end
  
  def detect(ifnone = nil, &block)
    java_debug "calling detect with #{ifnone}, #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:detect, ifnone) unless block_given?
    unnest_java_collections to_j.detect to_fn0(ifnone), to_fn1(block)
  end
  
  alias :find :detect

  def drop(n)
    begin
      java_debug "calling drop with #{n} on #{self.class}"
      raise TypeError, "can't convert #{n.class} into Integer" unless n.respond_to? :to_int
      unnest_java_collections to_j.drop(n.to_int)
    rescue Java::JavaLang::IllegalArgumentException => e
      raise ArgumentError, e.message
    end
  end
  
  def drop_while(&block)
    begin
      java_debug "calling drop_while with #{block_given? ? block : "<no block>"} on #{self.class}"
      return to_enum(:drop_while) unless block_given?
      unnest_java_collections to_j.drop_while to_fn1(block)
    rescue NativeException => e
      raise e.cause if e.cause.is_a? Java::OrgJrubyExceptions::JumpException
      raise
    end
  end
  
  def each_cons(n, &block)
    begin
      java_debug "calling each_cons with #{n}, #{block_given? ? block : "<no block>"} on #{self.class}"
      raise TypeError, "can't convert #{n.class} into Integer" unless n.respond_to? :to_int
      return to_enum(:each_cons, n.to_int) unless block_given?
      to_j.each_cons(n.to_int, to_fn1(block))
    rescue Java::JavaLang::IllegalArgumentException => e
      raise ArgumentError, e.message
    rescue NativeException => e
      raise e.cause if e.cause.is_a? Java::OrgJrubyExceptions::JumpException
      raise
    end
  end

  def each_slice(n, &block)
    begin
      java_debug "calling each_slice with #{n}, #{block_given? ? block : "<no block>"} on #{self.class}"
      raise TypeError, "can't convert #{n.class} into Integer" unless n.respond_to? :to_int
      return to_enum(:each_slice, n.to_int) unless block_given?
      to_j.each_slice(n.to_int, to_fn1(block))
    rescue Java::JavaLang::IllegalArgumentException => e
      raise ArgumentError, e.message
    rescue NativeException => e
      raise e.cause if e.cause.is_a? Java::OrgJrubyExceptions::JumpException
      raise
    end
  end
  
  def drop(n)
    begin
      java_debug "calling drop with #{n} on #{self.class}"
      raise TypeError, "can't convert #{n.class} into Integer" unless n.respond_to? :to_int
      unnest_java_collections to_j.drop(n.to_int)
    rescue Java::JavaLang::IllegalArgumentException => e
      raise ArgumentError, e.message
    end
  end
  
  def drop_while(&block)
    begin
      java_debug "calling drop_while with #{block_given? ? block : "<no block>"} on #{self.class}"
      return to_enum(:drop_while) unless block_given?
      unnest_java_collections to_j.drop_while to_fn1(block)
    rescue NativeException => e
      raise e.cause if e.cause.is_a? Java::OrgJrubyExceptions::JumpException
      raise
    end
  end
  
  def each_with_index(*args, &block)
    begin
      java_debug "calling each_with_index with #{args}, #{block_given? ? block : "<no block>"} on #{self.class}"
      unless block_given?
        a = []
        to_j.each_with_index(to_fn2(lambda {|o, i| a << [o, i]}))
        return a.to_enum
      end
      to_j.each_with_index(to_fn2(block))
      self
    rescue NativeException => e
      raise e.cause if e.cause.is_a? Java::OrgJrubyExceptions::JumpException
      raise
    end
  end

  def each_with_object(memo, &block)
    begin
      java_debug "calling each_with_object with #{memo}, #{block_given? ? block : "<no block>"} on #{self.class}"
      return to_enum(:each_with_object, memo) unless block_given?
      to_j.each_with_object(memo, to_fn2(block))
    rescue NativeException => e
      raise e.cause if e.cause.is_a? Java::OrgJrubyExceptions::JumpException
      raise
    end
  end
  
  def entries
    unnest_java_collections to_j.entries
  end

  def find_all(&block)
    java_debug "calling find_all with #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:find_all) unless block_given?
    unnest_java_collections to_j.find_all(to_fn1(block))
  end
  
  alias :select :find_all

  def find_index(obj = NotSupplied, &block)
    java_debug "calling find_index with #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:find_index) if !block_given? && obj == NotSupplied
    if obj == NotSupplied
      result = to_j.find_index(to_fn1(block))
    else
      STDERR.puts "warning: given block not used" if block_given?
      result = to_j.find_index(obj) unless obj == NotSupplied
    end
    result == -1 ? nil : result
  end

  def first(n = NotSupplied)
    begin
      java_debug "calling first with #{n} on #{self.class}"
      return to_j.first if n == NotSupplied
      raise TypeError, "can't convert #{n.class} into Integer" unless n.respond_to? :to_int
      unnest_java_collections to_j.first(n.to_int)
    rescue Java::JavaLang::IllegalArgumentException => e
      raise ArgumentError, e.message
    end
  end

# Real grep is used, as it relies on ducktyping for === and is not specific to regex.
#  def grep(pattern, &block)
#  end  

  def group_by(&block)
    java_debug "calling group_by with #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:group_by) unless block_given?
    unnest_java_collections to_j.group_by(to_fn1 block)
  end

  def include?(obj)
    java_debug "calling include? with #{obj} on #{self.class}"
    to_j.include(obj)
  end
  
  alias :member? :include?

  def inject(initial_or_symbol = NotSupplied, symbol = NotSupplied, &block)
    java_debug "calling inject with #{initial_or_symbol}, #{symbol}, #{block_given? ? block : "<no block>"} on #{self.class}"
    return unnest_java_collections to_j.inject(to_fn2(block)) if ((initial_or_symbol == NotSupplied) && block_given?)
    return unnest_java_collections to_j.inject(initial_or_symbol, to_fn2(block)) if ((symbol == NotSupplied) && block_given?)
    return unnest_java_collections to_j.inject(to_fn2(initial_or_symbol.to_proc)) if (symbol == NotSupplied)
    STDERR.puts "warning: given block not used" if block_given?
    unnest_java_collections to_j.inject(initial_or_symbol, to_fn2(symbol.to_proc))
  end
  
  alias :reduce :inject

  def max_by(&block)
    java_debug "calling max_by with #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:max_by) unless block_given?
    unnest_java_collections to_j.max_by(to_fn1(block))
  end

  def max(&block)
    begin
      java_debug "calling max with #{block_given? ? block : "<no block>"} on #{self.class}"
      return to_j.max unless block_given?
      unnest_java_collections to_j.max(to_fn2(block))
    rescue Java::JavaLang::ClassCastException, Java::JavaLang::NullPointerException => e
      raise ArgumentError, e.message
    end
  end

  def min_by(&block)
    java_debug "calling min_by with #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:min_by) unless block_given?
    unnest_java_collections to_j.min_by(to_fn1(block))
  end

  def min(&block)
    begin
      java_debug "calling min with #{block_given? ? block : "<no block>"} on #{self.class}"
      return to_j.min unless block_given?
      unnest_java_collections to_j.min(to_fn2(block))
    rescue Java::JavaLang::ClassCastException, Java::JavaLang::NullPointerException => e
      raise ArgumentError, e.message
    end
  end

  def minmax_by(&block)
    java_debug "calling minmax_by with #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:minmax_by) unless block_given?
    unnest_java_collections to_j.min_max_by(to_fn1(block))
  end

  def minmax(&block)
    begin
      java_debug "calling minmax with #{block_given? ? block : "<no block>"} on #{self.class}"
      return unnest_java_collections to_j.min_max unless block_given?
      unnest_java_collections to_j.min_max(to_fn2(block))
    rescue Java::JavaLang::ClassCastException, Java::JavaLang::NullPointerException => e
      raise ArgumentError, e.message
    end
  end

  def none?(&block)
    java_debug "calling none? with #{block_given? ? block : "<no block>"} on #{self.class}"
    to_j.none(to_fn1 block)
  end
  
  def one?(&block)
    java_debug "calling one? with #{block_given? ? block : "<no block>"} on #{self.class}"
    to_j.one(to_fn1 block)
  end

  def partition(&block)
    java_debug "calling partition with #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:partition) unless block_given?
    unnest_java_collections to_j.partition(to_fn1(block))
  end

  def reject(&block)
    java_debug "calling reject with #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:reject) unless block_given?
    unnest_java_collections to_j.reject(to_fn1(block))
  end


  def reverse_each(&block)
    java_debug "calling reverse_each with #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:reverse_each) unless block_given?
    to_j.reverse_each(to_fn1(block))
    self
  end

  def sort_by(&block)
    java_debug "calling sort_by with #{block_given? ? block : "<no block>"} on #{self.class}"
    return to_enum(:sort_by) unless block_given?
    unnest_java_collections to_j.sort_by(to_fn1(block))
  end

  def sort(&block)
    begin
      java_debug "calling sort with #{block_given? ? block : "<no block>"} on #{self.class}"
      return unnest_java_collections to_j.sort unless block_given?
      unnest_java_collections to_j.sort(to_fn2(block))
    rescue Java::JavaLang::ClassCastException, Java::JavaLang::NullPointerException => e
      raise ArgumentError, e.message
    end
  end

  def take(n)
    begin
      java_debug "calling take with #{n} on #{self.class}"
      raise TypeError, "can't convert #{n.class} into Integer" unless n.respond_to? :to_int
      unnest_java_collections to_j.take(n.to_int)
    rescue Java::JavaLang::IllegalArgumentException => e
      raise ArgumentError, e.message
    end
  end
  
  def take_while(&block)
    begin
      java_debug "calling take_while with #{block_given? ? block : "<no block>"} on #{self.class}"
      return to_enum(:take_while) unless block_given?
      unnest_java_collections to_j.take_while to_fn1(block)
    rescue NativeException => e
      raise e.cause if e.cause.is_a? Java::OrgJrubyExceptions::JumpException
      raise
    end
  end
  
  def zip(*arg, &block)
    java_debug "calling zip with #{arg}, #{block_given? ? block : "<no block>"} on #{self.class}"
    a = EnumerableModule.extend(Array == self.class || Hash == self.class ? self : internal_to_a)    
    return unnest_java_collections a.zip(arg, to_fn1(block)) if block_given?
    unnest_java_collections a.zip(*arg)
  end

  private
  class NotSupplied
  end
  
  def unnest_java_collections(o)
    if (o.is_a? Java::JavaUtil::List) && (o.class != Array)
      o.to_a.collect! {|e| unnest_java_collections e}
    elsif o.is_a? Java::JavaUtil::Map
      h = {}
      i = o.entry_set.iterator
      while i.has_next
        e = i.next
        h[e.key] = unnest_java_collections e.value 
      end
      h
    elsif o == EList
      Array
    elsif o == EMap
      Hash
    else
      o
    end
  end
end

module EnumerableJava
  def self.included(host)
    host.class_eval do
      java_debug "included EnumerableJava in #{host}"
      java_debug "undefining methods shadowing Enumerable on #{host}"
      instance_methods(false).select {|m| Enumerable.original_method? m}.each do |m|
        java_debug "removing #{m} on #{host}"
        remove_method m
      end      
    end
  end

  def to_a
    java_debug "calling to_a on #{self.class}"
    return self if is_a? Array
    internal_to_a
  end
  
  private
  def internal_to_a
    a = []
    each {|o| a << o }
    a
  end

  def to_j
    return EList.new(self) if is_a? Array
    return EMap.new(self) if is_a? Hash
    EIterable.new(self)
  end

  def to_fn0 proc
    return nil if proc.nil?
    JRubyTestBase.to_fn0 proc
  end

  def to_fn1 proc
    return Fn1.identity if proc.nil?
    JRubyTestBase.to_fn1 proc
  end

  def to_fn2 proc
    return nil if proc.nil?
    JRubyTestBase.to_fn2 proc
  end
end

class Range
  alias :original_include? :include?
  alias :original_first :first
  include EnumerableJava
  include Iterable
  def iterator
    EnumerableIterator.new(self)
  end

  def include? obj
    original_include? obj
  end
  
  def first
    original_first
  end
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

module java::lang::Iterable
  remove_method :each_with_index
end

