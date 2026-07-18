package app.dividends.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import app.dividends.client.TickerInformationRepository;
import app.dividends.client.TransactionRepository;
import app.dividends.client.marketdata.IMarketDataService;
import app.dividends.client.marketdata.model.TickerInformation;
import app.dividends.service.IPortfolioService;
import app.dividends.service.models.AbstractTransaction;
import app.dividends.service.models.Portfolio;
import app.dividends.service.models.Position;
import app.dividends.utils.PositionFactory;

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
