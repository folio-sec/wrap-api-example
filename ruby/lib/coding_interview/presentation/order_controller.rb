require_relative "presentation_exception"
require_relative "presentation_preparation"
require_relative "../application/usecase/order/new_contribution_order_usecase"
require_relative "../application/usecase/order/additional_buy_order_usecase"
require_relative "../application/usecase/order/rebalance_order_usecase"

module CodingInterview
  module Presentation
    NewContributionOrderRequest = Struct.new(:user_id, :amount)
    AdditionalContributionOrderRequest = Struct.new(:user_id, :amount)
    RebalanceOrderRequest = Struct.new(:user_id)

    class OrderController
      include PresentationPreparation

      def initialize(new_contribution_order_usecase, additional_buy_order_usecase, rebalance_order_usecase)
        @new_contribution_order_usecase = new_contribution_order_usecase
        @additional_buy_order_usecase = additional_buy_order_usecase
        @rebalance_order_usecase = rebalance_order_usecase
      end

      def new_contribution_order(req)
        uid = parse_user_id(req.user_id)
        amt = parse_amount(req.amount)
        @new_contribution_order_usecase.run(
          Application::Usecase::Order::NewContributionOrderUsecaseInput.new(uid, amt)
        )
      rescue Application::Usecase::Order::UserAlreadyExists
        raise BadRequestException.new("user already has account")
      rescue Application::Usecase::Order::NewContributionAmountTooSmall
        raise BadRequestException.new("amount is too small")
      end

      def additional_contribution_order(req)
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
