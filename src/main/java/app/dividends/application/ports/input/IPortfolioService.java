package app.dividends.application.ports.input;

import java.util.List;

import app.dividends.domain.model.Portfolio;
import app.dividends.domain.model.Position;

public interface IPortfolioService {

	public Portfolio calculatePortfolio();
}
