require "bigdecimal"
require_relative "presentation_exception"
require_relative "../domain/user_id"

module CodingInterview
  module Presentation
    module PresentationPreparation
      def parse_user_id(s)
        Domain::UserId.new(s)
      rescue ArgumentError => e
        raise BadRequestException.new(e.message)
      end

      def parse_amount(s)
        BigDecimal(s)
      rescue ArgumentError, TypeError
        raise BadRequestException.new("invalid amount: #{s}")
      end
    end
  end
end
