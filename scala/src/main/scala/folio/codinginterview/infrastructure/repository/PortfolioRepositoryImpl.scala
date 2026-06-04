package folio.codinginterview.infrastructure.repository

import folio.codinginterview.application.repository.PortfolioRepository
import folio.codinginterview.domain.AppConstants
import folio.codinginterview.domain.Portfolio
import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.Future

final class PortfolioRepositoryImpl extends PortfolioRepository {
  private val ref: AtomicReference[Portfolio] = new AtomicReference(AppConstants.initialPortfolio)

  override def get(): Future[Portfolio] = Future.successful(ref.get())

  override def update(portfolio: Portfolio): Future[Unit] = {
    ref.set(portfolio)
    Future.unit
  }
}
