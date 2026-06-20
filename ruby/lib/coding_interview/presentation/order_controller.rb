require_relative "presentation_exception"
require_relative "presentation_preparation"
require_relative "../application/usecase/order/new_order_usecase"
require_relative "../application/usecase/order/additional_buy_order_usecase"
require_relative "../application/usecase/order/rebalance_order_usecase"

module CodingInterview
  module Presentation
    NewOrderRequest = Struct.new(:user_id, :amount)
    AdditionalOrderRequest = Struct.new(:user_id, :amount)
    RebalanceOrderRequest = Struct.new(:user_id)

    class OrderController
      include PresentationPreparation

      def initialize(new_order_usecase, additional_buy_order_usecase, rebalance_order_usecase)
        @new_order_usecase = new_order_usecase
        @additional_buy_order_usecase = additional_buy_order_usecase
        @rebalance_order_usecase = rebalance_order_usecase
      end

      def new_order(req)
        uid = parse_user_id(req.user_id)
        amt = parse_amount(req.amount)
        @new_order_usecase.run(
          Application::Usecase::Order::NewOrderUsecaseInput.new(uid, amt)
        )
      rescue Application::Usecase::Order::NewOrderUserAlreadyExistsError
        raise BadRequestException.new("user already has account")
      rescue Application::Usecase::Order::NewOrderAmountTooSmallError
        raise BadRequestException.new("amount is too small")
      end

      def additional_order(req)
        uid = parse_user_id(req.user_id)
        amt = parse_amount(req.amount)
        @additional_buy_order_usecase.run(
          Application::Usecase::Order::AdditionalBuyOrderUsecaseInput.new(uid, amt)
        )
      rescue Application::Usecase::Order::AdditionalBuyUserNotFound
        raise BadRequestException.new("user has no live account")
      rescue Application::Usecase::Order::AdditionalBuyAmountTooSmall
        raise BadRequestException.new("amount is too small")
      end

      def rebalance_order(req)
        uid = parse_user_id(req.user_id)
        @rebalance_order_usecase.run(
          Application::Usecase::Order::RebalanceOrderUsecaseInput.new(uid)
        )
      rescue Application::Usecase::Order::RebalanceUserNotFound
        raise BadRequestException.new("user has no live account")
      end
    end
  end
end
