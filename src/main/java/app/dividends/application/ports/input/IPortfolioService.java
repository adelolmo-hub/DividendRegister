package app.dividends.application.ports.input;

import java.util.List;

import app.dividends.domain.model.Position;

public interface IPortfolioService {

	public List<Position> calculatePortfolio();
}
