require "bigdecimal"

RSpec.describe "Optimal Portfolio Management" do
  let(:server) { CodingInterview::Infrastructure::Server::DummyServer.default }
  let(:pc) { server.portfolio_controller }

  it "最適ポートフォリオを更新・取得できる" do
    pc.update_optimal_portfolio(
      CodingInterview::Presentation::UpdateOptimalPortfolioRequest.new([
        CodingInterview::Presentation::PortfolioItemDto.new("Toyopa", "0.20"),
        CodingInterview::Presentation::PortfolioItemDto.new("Somy", "0.80")
      ])
    )

    first = pc.get_optimal_portfolio
    first_map = first.portfolios.each_with_object({}) { |p, h| h[p.symbol] = p.rate }

    expect(BigDecimal(first_map["Toyopa"])).to eq(BigDecimal("0.20"))
    expect(BigDecimal(first_map["Somy"])).to eq(BigDecimal("0.80"))

    pc.update_optimal_portfolio(
      CodingInterview::Presentation::UpdateOptimalPortfolioRequest.new([
        CodingInterview::Presentation::PortfolioItemDto.new("Toyopa", "0.40"),
        CodingInterview::Presentation::PortfolioItemDto.new("Somy", "0.60")
      ])
    )
    second = pc.get_optimal_portfolio
    second_map = second.portfolios.each_with_object({}) { |p, h| h[p.symbol] = p.rate }

    expect(BigDecimal(second_map["Toyopa"])).to eq(BigDecimal("0.40"))
    expect(BigDecimal(second_map["Somy"])).to eq(BigDecimal("0.60"))
  end
end
