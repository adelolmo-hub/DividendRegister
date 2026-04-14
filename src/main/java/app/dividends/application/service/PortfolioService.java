package app.dividends.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.dividends.application.ports.input.IMarketDataService;
import app.dividends.application.ports.input.IPortfolioService;
import app.dividends.domain.model.Dividend;
import app.dividends.domain.model.DividendTransaction;
import app.dividends.domain.model.Order;
import app.dividends.domain.model.PortfolioDTO;
import app.dividends.domain.model.PortfolioSnapshot;
import app.dividends.domain.model.Position;
import app.dividends.domain.model.TickerInformation;
import app.dividends.domain.model.Transaction;
import app.dividends.infrastructure.persistence.PortfolioSnapshotRepository;
import app.dividends.infrastructure.persistence.TickerInformationRepository;
import app.dividends.infrastructure.persistence.TransactionRepository;

@Service
public class PortfolioService implements IPortfolioService{
	
	@Autowired
	private TransactionRepository transactionRepo;
	@Autowired
	private PortfolioSnapshotRepository snapshotRepository;
	
	@Autowired
	private IMarketDataService marketDataService;
	@Autowired
	private TickerInformationRepository tickerRepository;
	
	@Override
	public PortfolioDTO calculatePortfolio() {
		List<Position> positions = getAllPositions();
		BigDecimal totalDividends = BigDecimal.ZERO;
		BigDecimal totalValue = BigDecimal.ZERO;
		BigDecimal totalCost = BigDecimal.ZERO;
		BigDecimal totalValueWithDividends;
		BigDecimal totalProfit;
		BigDecimal totalProfitWithDividends;

 		for (Position p : positions) {
		    totalDividends = totalDividends.add(p.getTotalDividends());
		    totalValue = totalValue.add(p.getCurrentValueEUR().multiply(BigDecimal.valueOf(p.getQuantity())));
		    totalCost = totalCost.add(p.getAverageCost().multiply(BigDecimal.valueOf(p.getQuantity())));
		}
		totalValueWithDividends = totalValue.add(totalDividends);
		
		totalProfit = totalValue.subtract(totalCost)
				.divide(totalCost, RoundingMode.HALF_EVEN)
				.multiply(new BigDecimal("100"))
				.setScale(2, RoundingMode.HALF_EVEN);
		
		totalProfitWithDividends = totalValueWithDividends.subtract(totalCost)
				.divide(totalCost, RoundingMode.HALF_EVEN)
				.multiply(new BigDecimal("100"))
				.setScale(2, RoundingMode.HALF_EVEN);
		return new PortfolioDTO(positions, totalValue, totalValueWithDividends, totalProfit, totalProfitWithDividends, totalDividends);
	}
	
	private List<Position> getAllPositions() {
		List<Position> list;
		
		LocalDateTime lastUpdate= snapshotRepository.findTopByOrderByLastUpdateDesc()
				.map(PortfolioSnapshot::getLastUpdate)
				.orElse(LocalDateTime.MIN);
		
		if(lastUpdate.isBefore(LocalDateTime.now().minusDays(7))){
			List<Transaction> allTransactions = transactionRepo.findAllByOrderByDateAsc();
			
			Map<String, List<Transaction>> groupedByTicker = allTransactions.stream()
					.collect(Collectors.groupingBy(Transaction::getTicker));
			
			list =  groupedByTicker.entrySet().stream()
		            .map(entry -> calculatePosition(entry.getKey(), entry.getValue()))
		            .toList();
			
			updatePortfolioStorage(list);
		}else {
			list = snapshotRepository.findLastValueStored().stream()
					.map(snap -> new Position(
							snap.getTicker(),
							snap.getQuantity(),
							snap.getAverageCost(),
							getDividends(snap.getTicker()),
							snap.getCurrentValue(),
							snap.getCurrentValueEUR()
							)).toList();
		}
		
		return list;
	}
	
	private Position calculatePosition(String ticker, List<Transaction> transactions) {
		if (transactions == null || transactions.isEmpty()) {
	        return null;
	    }
		String currency = transactions.get(0).getCurrency();
		int currentQuantity = 0;
	    int totalPurchasedQuantity = 0;
	    BigDecimal totalCost = BigDecimal.ZERO;
	    List<Dividend> dividends = new ArrayList<>();
	    
	    BigDecimal txExchangeRate = marketDataService.getExchangeRate(currency);
		
	 // *********************PROCESAR TRANSACCIONES******************************
	    for (Transaction tx : transactions) {

	        if (tx instanceof Order order) {
	            if (order.getQuantity() > 0) {
	                currentQuantity += order.getQuantity();
	                totalPurchasedQuantity += order.getQuantity();
	                
	                BigDecimal orderCost = order.getPrice()
	                        .multiply(BigDecimal.valueOf(order.getQuantity()))
	                        .multiply(txExchangeRate);
	                totalCost = totalCost.add(orderCost);
	                totalCost = totalCost.subtract(order.getCommission());
	            } else {
	                // Venta
	                currentQuantity += order.getQuantity();
	            }
	        } else if (tx instanceof DividendTransaction divTx) {
	            dividends.add(mapToDividend(divTx, txExchangeRate));
	        }
	    }
	    
	    // **********************OBTENER INFORMACION DEL MERCADO*****************************
		String fullTickerString = fullTickerString(ticker);
		BigDecimal currentValue = marketDataService.getCurrentAssetValue(fullTickerString);
		
		// **************Normalizacion de moneda**************************
		BigDecimal currentValueEUR = currentValue.multiply(txExchangeRate);
		
		// ************************Calculo de promedio**********************
		BigDecimal averageCost = new BigDecimal("0");
		if (totalPurchasedQuantity != 0) {
			averageCost = totalCost.divide(BigDecimal.valueOf(totalPurchasedQuantity), 2, RoundingMode.HALF_EVEN);
		}
		return new Position(ticker, currentQuantity, averageCost, dividends, currentValue, currentValueEUR);
	}
	
	
	public void updatePortfolioStorage(List<Position> positions) {
		for(Position position : positions) {
			PortfolioSnapshot snapshot = new PortfolioSnapshot();
			snapshot.setTicker(position.getTicker());
			snapshot.setQuantity(position.getQuantity());
			snapshot.setAverageCost(position.getAverageCost());
			snapshot.setCurrentValue(position.getCurrentValue());
			snapshot.setCurrentValueEUR(position.getCurrentValueEUR());
			snapshot.setTotalValue(position.getTotalValue());
			snapshot.setProfit(position.getProfit());
			snapshot.setTotalProfit(position.getTotalProfit());
			snapshot.setTotalProfitWithDividends(position.getTotalProfitWithDividends());
			snapshot.setLastUpdate(LocalDateTime.now());
			
			snapshotRepository.save(snapshot);
		}
	}
	
	private Dividend mapToDividend(DividendTransaction divTx, BigDecimal exchangeRate) {
	    Dividend d = new Dividend();
	    d.setDate(divTx.getDate());
	    d.setCurrency(divTx.getCurrency());
	    d.setQuantityPerAsset(Objects.requireNonNullElse(divTx.getPrice(), BigDecimal.ZERO));
	    d.setTotalRecievedInEur(divTx.getAmountReceived().multiply(exchangeRate));
	    return d;
	}
	
	private String fullTickerString(String ticker) {
		TickerInformation tickerInfo;
		if((tickerInfo = tickerRepository.findByTicker(ticker)) != null) {
			return tickerInfo.getTicker() + Optional.ofNullable(
					tickerInfo.getSufix()).orElse("");
		}else {
			return ticker;
		}
	}
	
	private List<Dividend> getDividends(String ticker){
		List<Dividend> dividends = new ArrayList<>();
		List<DividendTransaction> transactions = transactionRepo.findAllByTicker(ticker);
		if(transactions.size() == 0) {
			return dividends;
		}
		
		BigDecimal exchangeRate = marketDataService.getExchangeRate(transactions.get(0).getCurrency());
		for(DividendTransaction tx : transactions) {
			dividends.add(mapToDividend(tx, exchangeRate));
		}
		return dividends;
	}
	
	
}
