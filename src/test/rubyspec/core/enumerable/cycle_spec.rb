require File.expand_path('../../../spec_helper', __FILE__)
require File.expand_path('../fixtures/classes', __FILE__)

describe "Enumerable#cycle" do
  ruby_version_is '1.8.7' do

    it "loops indefinitely if no argument or nil argument" do
      [[],[nil]].each do |args|
        bomb = 10
        EnumerableSpecs::Numerous.new.cycle(*args) do
          bomb -= 1
          break 42 if bomb <= 0
        end.should == 42
        bomb.should == 0
      end
    end

    it "yields successive elements of the array repeatedly" do
      b = []
      EnumerableSpecs::Numerous.new(1,2,3).cycle do |elem|
        b << elem
        break if b.size == 7
      end
      b.should == [1,2,3,1,2,3,1]
    end

    describe "passed a number n as an argument" do
      it "returns nil and does nothing for non positive n" do
        EnumerableSpecs::ThrowingEach.new.cycle(0){}.should be_nil
        EnumerableSpecs::NoEach.new.cycle(-22){}.should be_nil
      end

      it "calls each at most once" do
        enum = EnumerableSpecs::EachCounter.new(1, 2)
        enum.cycle(3).to_a.should == [1,2,1,2,1,2]
        enum.times_called.should == 1
      end

      it "yields only when necessary" do
        enum = EnumerableSpecs::EachCounter.new(10, 20, 30)
        enum.cycle(3){|x| break if x == 20}
        enum.times_yielded.should == 2
      end

      it "tries to convert n to an Integer using #to_int" do
        enum = EnumerableSpecs::Numerous.new(3, 2, 1)
        enum.cycle(2.3).to_a.should == [3, 2, 1, 3, 2, 1]

        obj = mock('to_int')
        obj.should_receive(:to_int).and_return(2)
        enum.cycle(obj).to_a.should == [3, 2, 1, 3, 2, 1]
      end

      it "raises a TypeError when the passed n can be coerced to Integer" do
        enum = EnumerableSpecs::Numerous.new
        lambda{ enum.cycle("cat"){} }.should raise_error(TypeError)
      end

      it "raises an ArgumentError if more arguments are passed" do
        enum = EnumerableSpecs::Numerous.new
        lambda{ enum.cycle(1, 2){} }.should raise_error(ArgumentError)
      end
    end
  end
end
