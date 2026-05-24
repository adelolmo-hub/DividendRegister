package app.dividends.domain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import app.dividends.application.ports.input.IMarketDataService;
import app.dividends.application.ports.input.IPortfolioService;
import app.dividends.domain.model.AbstractTransaction;
import app.dividends.domain.model.Portfolio;
import app.dividends.domain.model.Position;
import app.dividends.domain.model.TickerInformation;
import app.dividends.domain.model.utils.PositionFactory;
import app.dividends.infrastructure.persistence.TickerInformationRepository;
import app.dividends.infrastructure.persistence.TransactionRepository;

@Service
public class PortfolioService implements IPortfolioService{
	
	private final TransactionRepository transactionRepo;
	private final TickerInformationRepository tickerInfoRepo;
	private final IMarketDataService marketDataService;

	public PortfolioService(
			TransactionRepository transactionRepo, 
			TickerInformationRepository tickerInfoRepo,
			IMarketDataService marketDataService) {
		this.transactionRepo = transactionRepo;
		this.tickerInfoRepo = tickerInfoRepo;
		this.marketDataService = marketDataService;
	}

	@Override
	public Portfolio calculatePortfolio() {
		List<Position> positions = getAllPositions();
		return new Portfolio(positions);
	}
	
	
	private List<Position> getAllPositions() {	
		List<AbstractTransaction> allTransactions = transactionRepo.findAllByOrderByDateAsc();
		
		if (allTransactions.isEmpty()) {
			return List.of();
		}
			
		Map<String, List<AbstractTransaction>> groupedByTicker = allTransactions.stream()
					.collect(Collectors.groupingBy(AbstractTransaction::getTicker));
			
		List<Position> calculatedPositions = groupedByTicker.entrySet().stream()
					.map(entry -> {
						String ticker = entry.getKey();
						List<AbstractTransaction> txs = entry.getValue();
						
						BigDecimal exchangeRate = marketDataService.getExchangeRate(txs.get(0).getCurrency());
						
						TickerInformation t = tickerInfoRepo.findByTicker(ticker);
						BigDecimal currentAssetValue = BigDecimal.ZERO;
						
						if (t != null) {
							String fullTickerString = ticker + Objects.toString(t.getSufix(), "");
							currentAssetValue = marketDataService.getCurrentAssetValue(fullTickerString);
						}
						
						return PositionFactory.createPosition(ticker, currentAssetValue, exchangeRate, txs);
					})
					.toList();
		
		return calculatedPositions;
	}
}
