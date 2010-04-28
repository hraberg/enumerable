require File.expand_path('../../../spec_helper', __FILE__)
require File.expand_path('../fixtures/classes', __FILE__)

describe "Enumerable#sort_by" do
  it "returns an array of elements ordered by the result of block" do
    a = EnumerableSpecs::Numerous.new("once", "upon", "a", "time")
    a.sort_by { |i| i[0] }.should == ["a", "once", "time", "upon"]
  end

  it "sorts the object by the given attribute" do
    a = EnumerableSpecs::SortByDummy.new("fooo")
    b = EnumerableSpecs::SortByDummy.new("bar")

    ar = [a, b].sort_by { |d| d.s }
    ar.should == [b, a]
  end

  ruby_version_is "1.8.7" do
    it "returns an Enumerator when a block is not supplied" do
      a = EnumerableSpecs::Numerous.new("a","b")
      a.sort_by.should be_an_instance_of(enumerator_class)
      a.to_a.should == ["a", "b"]
    end
  end
end
