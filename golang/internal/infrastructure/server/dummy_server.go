package server

import (
	assetusecase "folio/codinginterview/internal/application/usecase/asset"
	"folio/codinginterview/internal/application/usecase/order"
	portfoliousecase "folio/codinginterview/internal/application/usecase/portfolio"
	infrarepo "folio/codinginterview/internal/infrastructure/repository"
	"folio/codinginterview/internal/presentation"
)

type DummyServer struct {
	AssetController     *presentation.AssetController
	PortfolioController *presentation.PortfolioController
	OrderController     *presentation.OrderController
}

func NewDefaultDummyServer() *DummyServer {
	portfolioRepo := infrarepo.NewPortfolioRepositoryImpl()
	accountRepo := infrarepo.NewAccountRepositoryImpl()

	getAssetUsecase := assetusecase.NewGetAssetUsecase(accountRepo)
	getLatestPortfolioUsecase := portfoliousecase.NewGetLatestPortfolioUsecase(portfolioRepo)
	updatePortfolioUsecase := portfoliousecase.NewUpdatePortfolioUsecase(portfolioRepo)
	newOrderUsecase := order.NewNewOrderUsecase(accountRepo, portfolioRepo)
	additionalBuyOrderUsecase := order.NewAdditionalBuyOrderUsecase(accountRepo, portfolioRepo)
	rebalanceOrderUsecase := order.NewRebalanceOrderUsecase(accountRepo, portfolioRepo)

	assetController := presentation.NewAssetController(getAssetUsecase)
	portfolioController := presentation.NewPortfolioController(getLatestPortfolioUsecase, updatePortfolioUsecase)
	orderController := presentation.NewOrderController(newOrderUsecase, additionalBuyOrderUsecase, rebalanceOrderUsecase)

	return &DummyServer{
		AssetController:     assetController,
		PortfolioController: portfolioController,
		OrderController:     orderController,
	}
}
