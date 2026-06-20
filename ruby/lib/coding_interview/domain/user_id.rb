module CodingInterview
  module Domain
    # ユーザーIDを表す。
    class UserId
      attr_reader :value

      def initialize(value)
        raise ArgumentError, "userId must not be empty" if value.nil? || value.empty?
        @value = value
      end

      def ==(other)
        other.is_a?(UserId) && other.value == @value
      end
      alias eql? ==

      def hash
        @value.hash
      end

      def to_s
        @value
      end
    end
  end
end
