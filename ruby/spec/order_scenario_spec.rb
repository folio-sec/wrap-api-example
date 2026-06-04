require "bigdecimal"
require "securerandom"
require "set"

RSpec.describe "Investment Operation" do
  let(:server) { CodingInterview::Infrastructure::Server::DummyServer.default }
  let(:ac) { server.asset_controller }
  let(:pc) { server.portfolio_controller }
  let(:oc) { server.order_controller }
  let(:mp) { server.market_price_controller }

  before do
    pc.update_optimal_portfolio(
      CodingInterview::Presentation::UpdateOptimalPortfolioRequest.new([
        CodingInterview::Presentation::PortfolioItemDto.new("Toyopa", "0.40"),
        CodingInterview::Presentation::PortfolioItemDto.new("Somy", "0.60")
      ])
    )
    mp.update_market_price(
      CodingInterview::Presentation::UpdateMarketPriceRequest.new([
        CodingInterview::Presentation::MarketPriceItemDto.new("Toyopa", "2.5"),
        CodingInterview::Presentation::MarketPriceItemDto.new("Somy", "3.0")
      ])
    )
  end

  it "新規拠出・追加拠出・リバランスの一連の操作が正しく機能する" do
    user_id = SecureRandom.uuid

    # Given: 存在しないユーザーで資産を取得しようとする
    expect {
      ac.get_asset(CodingInterview::Presentation::GetAssetRequest.new(user_id))
    }.to raise_error(CodingInterview::Presentation::BadRequestException)
    # Then: BadRequestException が返される

    # When: 最適ポートフォリオを Toyopa=40%, Somy=60% に更新する
    pc.update_optimal_portfolio(
      CodingInterview::Presentation::UpdateOptimalPortfolioRequest.new([
        CodingInterview::Presentation::PortfolioItemDto.new("Toyopa", "0.40"),
        CodingInterview::Presentation::PortfolioItemDto.new("Somy", "0.60")
      ])
    )

    # And: 新規拠出を 100,000 円で注文する
    oc.new_contribution_order(
      CodingInterview::Presentation::NewContributionOrderRequest.new(user_id, "100000")
    )

    asset1 = ac.get_asset(CodingInterview::Presentation::GetAssetRequest.new(user_id))
    expect(asset1.stocks.map(&:symbol).to_set).to eq(Set["Toyopa", "Somy"])
    total1 = BigDecimal(asset1.cash_amount) + asset1.stocks.inject(BigDecimal("0")) { |a, e| a + BigDecimal(e.evaluation_amount) }
    expect((total1 - BigDecimal("100000")).abs).to be <= BigDecimal("2")

    # Then: 現金比率5%に対して現金が 5,000円、最適ポートフォリオに基づき Toyopa の評価額が 38,000 円(40%)、Somy の評価額が 57,000 円(60%) となる
    asset1_toyopa = asset1.stocks.find { |e| e.symbol == "Toyopa" }
    asset1_somy = asset1.stocks.find { |e| e.symbol == "Somy" }
    expect(BigDecimal(asset1_toyopa.evaluation_amount)).to eq(BigDecimal("38000"))
    expect(BigDecimal(asset1_somy.evaluation_amount)).to eq(BigDecimal("57000"))
    expect(BigDecimal(asset1.cash_amount)).to eq(BigDecimal("5000"))

    # When: 追加拠出を 100,000 円で注文する
    oc.additional_contribution_order(
      CodingInterview::Presentation::AdditionalContributionOrderRequest.new(user_id, "100000")
    )

    # Then: 資産合計が約 200,000 円になる
    asset2 = ac.get_asset(CodingInterview::Presentation::GetAssetRequest.new(user_id))
    total2 = BigDecimal(asset2.cash_amount) + asset2.stocks.inject(BigDecimal("0")) { |a, e| a + BigDecimal(e.evaluation_amount) }
    expect((total2 - BigDecimal("200000")).abs).to be <= BigDecimal("4")

    # And: 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 76,000 円(40%)、Somy の評価額が 114,000 円(60%) となる
    asset2_toyopa = asset2.stocks.find { |e| e.symbol == "Toyopa" }
    asset2_somy = asset2.stocks.find { |e| e.symbol == "Somy" }
    expect(BigDecimal(asset2_toyopa.evaluation_amount)).to eq(BigDecimal("76000"))
    expect(BigDecimal(asset2_somy.evaluation_amount)).to eq(BigDecimal("114000"))
    expect(BigDecimal(asset2.cash_amount)).to eq(BigDecimal("10000"))

    # When: 最適ポートフォリオを Toyopa=10%, Somy=90% に変更して、リバランス注文をする
    pc.update_optimal_portfolio(
      CodingInterview::Presentation::UpdateOptimalPortfolioRequest.new([
        CodingInterview::Presentation::PortfolioItemDto.new("Toyopa", "0.10"),
        CodingInterview::Presentation::PortfolioItemDto.new("Somy", "0.90")
      ])
    )
    oc.rebalance_order(CodingInterview::Presentation::RebalanceOrderRequest.new(user_id))

    # Then: リバランス後も資産合計がほぼ変わらない
    asset3 = ac.get_asset(CodingInterview::Presentation::GetAssetRequest.new(user_id))
    total3 = BigDecimal(asset3.cash_amount) + asset3.stocks.inject(BigDecimal("0")) { |a, e| a + BigDecimal(e.evaluation_amount) }
    expect((total3 - total2).abs).to be <= BigDecimal("4")

    # And: 現金比率5%に対して現金が 10,000円、最適ポートフォリオに基づき Toyopa の評価額が 19,000 円(10%)、Somy の評価額が 171,000 円(90%) となる
    asset3_toyopa = asset3.stocks.find { |e| e.symbol == "Toyopa" }
    asset3_somy = asset3.stocks.find { |e| e.symbol == "Somy" }
    expect(BigDecimal(asset3_toyopa.evaluation_amount)).to eq(BigDecimal("19000"))
    expect(BigDecimal(asset3_somy.evaluation_amount)).to eq(BigDecimal("171000"))
    expect(BigDecimal(asset3.cash_amount)).to eq(BigDecimal("10000"))
  end
end
