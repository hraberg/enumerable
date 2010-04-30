require File.expand_path('../../../spec_helper', __FILE__)
require File.expand_path('../fixtures/classes', __FILE__)

describe "Enumerable#grep" do
  before(:each) do
    @a = EnumerableSpecs::EachDefiner.new( 2, 4, 6, 8, 10)
  end
  
  platform_is_not :enumerable_java do
    it "grep without a block should return an array of all elements === pattern" do
      class EnumerableSpecGrep; def ===(obj); obj == '2'; end; end

      EnumerableSpecs::Numerous.new('2', 'a', 'nil', '3', false).grep(EnumerableSpecGrep.new).should == ['2']
    end
  end
  
  platform_is_not :enumerable_java do
    it "grep with a block should return an array of elements === pattern passed through block" do
      class EnumerableSpecGrep2; def ===(obj); /^ca/ =~ obj; end; end

      EnumerableSpecs::Numerous.new("cat", "coat", "car", "cadr", "cost").grep(EnumerableSpecGrep2.new) { |i| i.upcase }.should == ["CAT", "CAR", "CADR"]
    end 
  end
  
  platform_is_not :enumerable_java do  
    it "grep the enumerable (rubycon legacy)" do 
      EnumerableSpecs::EachDefiner.new().grep(1).should == []
      @a.grep(3..7).should == [4,6]
      @a.grep(3..7) {|a| a+1}.should == [5,7]
    end
  end
  
  platform_is_not :enumerable_java do
    it "can use $~ in the block when used with a Regexp" do
      ary = ["aba", "aba"]
      ary.grep(/a(b)a/) { $1 }.should == ["b", "b"]
    end
  end
end
