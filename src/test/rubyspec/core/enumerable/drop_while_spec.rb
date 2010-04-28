require File.expand_path('../../../spec_helper', __FILE__)
require File.expand_path('../fixtures/classes', __FILE__)

describe "Enumerable#drop_while" do
  ruby_version_is '1.8.7' do
    before :each do
      @enum = EnumerableSpecs::Numerous.new(3, 2, 1, :go)
    end

    it 'returns an Enumerator if no block given' do
      @enum.drop_while.should be_an_instance_of(enumerator_class)
    end

    it "returns no/all elements for {true/false} block" do
      @enum.drop_while{true}.should == []
      @enum.drop_while{false}.should == @enum.to_a
    end
    
    it "accepts returns other than true/false" do
      @enum.drop_while{1}.should == []
      @enum.drop_while{nil}.should == @enum.to_a
    end
  
    it "passes elements to the block until the first false" do
      a = []
      @enum.drop_while{|obj| (a << obj).size < 3}.should == [1, :go]
      a.should == [3, 2, 1]
    end

    it "will only go through what's needed" do
      enum = EnumerableSpecs::EachCounter.new(1,2,3,4)
      enum.drop_while { |x|
        break 42 if x == 3
        true
      }.should == 42
      enum.times_yielded.should == 3
    end

    it "doesn't return self when it could" do
      a = [1,2,3]
      a.drop_while{false}.should_not equal(a)
    end
  end
end
