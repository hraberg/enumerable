require 'mspec/runner/mspec'

class Object
  def it_behaves_like(desc, meth, obj=nil)
    send :before, :all do
      @method = meth
      @object = obj if obj
    end

    send :it_should_behave_like, desc.to_s
  end
end
