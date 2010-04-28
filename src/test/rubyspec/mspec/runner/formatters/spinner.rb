require 'mspec/expectations/expectations'
require 'mspec/runner/formatters/dotted'

class SpinnerFormatter < DottedFormatter
  attr_reader :length

  Spins = %w!| / - \\!
  HOUR = 3600
  MIN = 60

  def initialize(out=nil)
    @exception = @failure = false
    @exceptions = []
    @count = 0
    @out = $stdout

    @which = 0
    @loaded = 0
    self.length = 40
    @percent = 0
    @start = Time.now

    term = ENV['TERM']
    @color = (term != "dumb")
    @fail_color  = "32"
    @error_color = "32"
  end

  def register
    super

    MSpec.register :start, self
    MSpec.register :load, self
  end

  def length=(length)
    @length = length
    @ratio = 100.0 / length
    @position = length / 2 - 2
  end

  def etr
    return "00:00:00" if @percent == 0
    elapsed = Time.now - @start
    remain = (100 * elapsed / @percent) - elapsed

    hour = remain >= HOUR ? (remain / HOUR).to_i : 0
    remain -= hour * HOUR
    min = remain >= MIN ? (remain / MIN).to_i : 0
    sec = remain - min * MIN

    "%02d:%02d:%02d" % [hour, min, sec]
  end

  def percentage
    @percent = @loaded * 100 / @total
    bar = ("=" * (@percent / @ratio)).ljust @length
    label = "%d%%" % @percent
    bar[@position, label.size] = label
    bar
  end

  def spin
    @which = (@which + 1) % Spins.size
    if @color
      print "\r[%s | %s | %s] \033[0;#{@fail_color}m%6dF \033[0;#{@error_color}m%6dE\033[0m" %
          [Spins[@which], percentage, etr, @counter.failures, @counter.errors]
    else
      print "\r[%s | %s | %s] %6dF %6dE" %
          [Spins[@which], percentage, etr, @counter.failures, @counter.errors]
    end
  end

  # Callback for the MSpec :start event. Stores the total
  # number of files that will be processed.
  def start
    @total = MSpec.retrieve(:files).size
  end

  # Callback for the MSpec :load event. Increments the number
  # of files that have been loaded.
  def load
    @loaded += 1
  end

  # Callback for the MSpec :exception event. Changes the color
  # used to display the tally of errors and failures
  def exception(exception)
    super
    @fail_color =  "31" if exception.failure?
    @error_color = "33" unless exception.failure?
  end

  # Callback for the MSpec :after event. Updates the spinner
  # and progress bar.
  def after(state)
    spin
  end
end
