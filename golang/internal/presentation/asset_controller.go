package presentation

import (
	"errors"

	"folio/codinginterview/internal/application/usecase/asset"
)

type StockDto struct {
	Symbol           string
	EvaluationAmount string
}

type GetAssetRequest struct {
	UserId string
}

type GetAssetResponse struct {
	CashAmount string
	Stocks     []StockDto
}

type AssetController struct {
	getAssetUsecase *asset.GetAssetUsecase
}

func NewAssetController(getAssetUsecase *asset.GetAssetUsecase) *AssetController {
	return &AssetController{getAssetUsecase: getAssetUsecase}
}

func (c *AssetController) GetAsset(req GetAssetRequest) (GetAssetResponse, error) {
	uid, err := parseUserId(req.UserId)
	if err != nil {
		return GetAssetResponse{}, err
	}

	out, err := c.getAssetUsecase.Run(asset.GetAssetUsecaseInput{UserId: uid})
	if err != nil {
		if errors.Is(err, asset.ErrUserNotFound) {
			return GetAssetResponse{}, newBadRequest("user not found")
		}
		return GetAssetResponse{}, err
	}

	stocks := make([]StockDto, 0, len(out.Stocks))
	for _, s := range out.Stocks {
		stocks = append(stocks, StockDto{
			Symbol:           string(s.Symbol),
			EvaluationAmount: s.EvaluationAmount.String(),
		})
	}
	return GetAssetResponse{
		CashAmount: out.CashAmount.String(),
		Stocks:     stocks,
	}, nil
}
