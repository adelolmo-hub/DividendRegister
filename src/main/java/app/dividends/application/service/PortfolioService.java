package app.dividends.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
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
import app.dividends.domain.model.Position;
import app.dividends.domain.model.TickerInformation;
import app.dividends.domain.model.Transaction;
import app.dividends.infrastructure.persistence.TickerInformationRepository;
import app.dividends.infrastructure.persistence.TransactionRepository;

@Service
public class PortfolioService implements IPortfolioService{
	
	@Autowired
	private TransactionRepository repo;
	
	@Autowired
	private IMarketDataService marketDataService;
	@Autowired
	private TickerInformationRepository tickerRepository;

	@Override
	public List<Position> calculatePortfolio() {
		
		List<Transaction> allTransactions = repo.findAllByOrderByDateAsc();
		
		Map<String, List<Transaction>> groupedByTicker = allTransactions.stream()
				.collect(Collectors.groupingBy(Transaction::getTicker));
		
		//No se crea entrada para mondi
		List<Position> list =  groupedByTicker.entrySet().stream()
	            .map(entry -> calculatePosition(entry.getKey(), entry.getValue()))
	            .filter(pos -> pos.getQuantity() > 0)
	            .toList();
		return list;
	}

	private Position calculatePosition(String ticker, List<Transaction> transactions) {
		int quantity = 0;
		int absoluteQuantity = 0;
		BigDecimal totalCost = new BigDecimal("0");
		BigDecimal currentValue = new BigDecimal("0");
		
		List<Dividend> dividends = new LinkedList<>();
		
		for(Transaction tx : transactions) {
			if(tx instanceof Order order) {
				if(order.getQuantity() > 0){ 
					//Compra
					quantity += order.getQuantity();
					absoluteQuantity += order.getQuantity();
					totalCost = totalCost.add(order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity())));
				}else { 
					//Venta
					quantity += order.getQuantity();
				}
			}else if(tx instanceof DividendTransaction divTx){
				//Dividendo
				Dividend d = new Dividend();
				d.setDate(divTx.getDate());
				d.setCurrency(divTx.getCurrency());
				d.setQuantityPerAsset(Objects.requireNonNullElse(divTx.getPrice(), BigDecimal.ZERO));
				d.setTotalRecieved(divTx.getAmountReceived());
				dividends.add(d);
			}
		}
		TickerInformation tickerInfo;
		String fullTickerString;
		if((tickerInfo = tickerRepository.findByTicker(ticker)) != null) {
			fullTickerString = tickerInfo.getTicker() + Optional.ofNullable(tickerInfo.getSufix()).orElse("");
		}else {
			fullTickerString = ticker;
		}
		currentValue = marketDataService.getCurrentAssetValue(fullTickerString);
		
		BigDecimal averageCost = new BigDecimal("0");
		if (absoluteQuantity != 0) {
			averageCost = totalCost.divide(BigDecimal.valueOf(absoluteQuantity), 2, RoundingMode.HALF_EVEN);
		}
		return new Position(ticker, quantity, averageCost, dividends, currentValue);
	}
	
	
	
}
