package server

import (
	marketprice "folio/codinginterview/internal/application/usecase/market_price"
	"folio/codinginterview/internal/application/usecase/order"
	portfoliousecase "folio/codinginterview/internal/application/usecase/portfolio"
	assetusecase "folio/codinginterview/internal/application/usecase/asset"
	infrarepo "folio/codinginterview/internal/infrastructure/repository"
	"folio/codinginterview/internal/presentation"
)

type DummyServer struct {
	AssetController       *presentation.AssetController
	PortfolioController   *presentation.PortfolioController
	OrderController       *presentation.OrderController
	MarketPriceController *presentation.MarketPriceController
}

func NewDefaultDummyServer() *DummyServer {
	portfolioRepo := infrarepo.NewPortfolioRepositoryImpl()
	accountRepo := infrarepo.NewAccountRepositoryImpl()
	marketPriceRepo := infrarepo.NewMarketPriceRepositoryImpl()

	getAssetUsecase := assetusecase.NewGetAssetUsecase(accountRepo, marketPriceRepo)
	getLatestPortfolioUsecase := portfoliousecase.NewGetLatestPortfolioUsecase(portfolioRepo)
	updatePortfolioUsecase := portfoliousecase.NewUpdatePortfolioUsecase(portfolioRepo)
	updateMarketPriceUsecase := marketprice.NewUpdateMarketPriceUsecase(marketPriceRepo)
	newContributionOrderUsecase := order.NewNewContributionOrderUsecase(accountRepo, portfolioRepo, marketPriceRepo)
	additionalBuyOrderUsecase := order.NewAdditionalBuyOrderUsecase(accountRepo, portfolioRepo, marketPriceRepo)
	rebalanceOrderUsecase := order.NewRebalanceOrderUsecase(accountRepo, portfolioRepo, marketPriceRepo)

	assetController := presentation.NewAssetController(getAssetUsecase)
	portfolioController := presentation.NewPortfolioController(getLatestPortfolioUsecase, updatePortfolioUsecase)
	orderController := presentation.NewOrderController(newContributionOrderUsecase, additionalBuyOrderUsecase, rebalanceOrderUsecase)
	marketPriceController := presentation.NewMarketPriceController(updateMarketPriceUsecase)

	return &DummyServer{
		AssetController:       assetController,
		PortfolioController:   portfolioController,
		OrderController:       orderController,
		MarketPriceController: marketPriceController,
	}
}
