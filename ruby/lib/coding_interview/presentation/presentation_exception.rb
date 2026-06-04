module CodingInterview
  module Presentation
    class PresentationException < StandardError; end

    class BadRequestException < PresentationException
      def initialize(message)
        super(message)
      end
    end
  end
end
