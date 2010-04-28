require File.expand_path('../../../spec_helper', __FILE__)
require File.expand_path('../fixtures/classes', __FILE__)

describe "Enumerable#minmax_by" do
  ruby_version_is '1.8.7' do
    it "returns an enumerator if no block" do
      EnumerableSpecs::Numerous.new(42).minmax_by.should be_an_instance_of(enumerator_class)
    end

    it "returns nil if #each yields no objects" do
      EnumerableSpecs::Empty.new.minmax_by {|o| o.nonesuch }.should == [nil, nil]
    end

    it "returns the object for whom the value returned by block is the largest" do
      EnumerableSpecs::Numerous.new(*%w[1 2 3]).minmax_by {|obj| obj.to_i }.should == ['1', '3']
      EnumerableSpecs::Numerous.new(*%w[three five]).minmax_by {|obj| obj.length }.should == ['five', 'three']
    end

    it "returns the object that appears first in #each in case of a tie" do
      a, b, c, d = '1', '1', '2', '2'
      mm = EnumerableSpecs::Numerous.new(a, b, c, d).minmax_by {|obj| obj.to_i }
      mm[0].should equal(a)
      mm[1].should equal(c)
    end

    it "uses min/max.<=>(current) to determine order" do
      a, b, c = (1..3).map{|n| EnumerableSpecs::ReverseComparable.new(n)}

      # Just using self here to avoid additional complexity
      EnumerableSpecs::Numerous.new(a, b, c).minmax_by {|obj| obj }.should == [c, a]
    end

    it "is able to return the maximum for enums that contain nils" do
      enum = EnumerableSpecs::Numerous.new(nil, nil, true)
      enum.minmax_by {|o| o.nil? ? 0 : 1 }.should == [nil, true]
    end
  end
end