package app.dividends.application.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.dividends.application.ports.input.IPortfolioService;
import app.dividends.domain.model.Dividend;
import app.dividends.domain.model.DividendTransaction;
import app.dividends.domain.model.Order;
import app.dividends.domain.model.Position;
import app.dividends.domain.model.Transaction;
import app.dividends.infrastructure.persistence.TransactionRepository;

@Service
public class PortfolioService implements IPortfolioService{
	
	@Autowired
	private TransactionRepository repo;

	@Override
	public List<Position> calculatePortfolio() {
		
		List<Transaction> allTransactions = repo.findAllByOrderByDateAsc();
		
		Map<String, List<Transaction>> groupedByTicker = allTransactions.stream()
				.collect(Collectors.groupingBy(Transaction::getTicker));
		
		
		return groupedByTicker.entrySet().stream()
	            .map(entry -> calculatePosition(entry.getKey(), entry.getValue()))
	            .filter(pos -> pos.getQuantity() > 0) 
	            .toList();
	}

	private Position calculatePosition(String ticker, List<Transaction> transactions) {
		int quantity = 0;
		int absoluteQuantity = 0;
		double totalCost = 0;
		
		List<Dividend> dividends = new LinkedList<>();
		
		for(Transaction tx : transactions) {
			if(tx instanceof Order order) {
				if(order.getQuantity() > 0){
					quantity += order.getQuantity();
					absoluteQuantity += order.getQuantity();
					totalCost += order.getPrice() * order.getQuantity();
				}else {
					quantity -= order.getQuantity();
				}
			}else if(tx instanceof DividendTransaction divTx){
				Dividend d = new Dividend();
				d.setDate(divTx.getDate());
				d.setCurrency(divTx.getCurrency());
				d.setQuantityPerAsset(Objects.requireNonNullElse(divTx.getPrice(), 0.0));
				d.setTotalRecieved(divTx.getQuantity());
				dividends.add(d);
			}
		}
		
		double averageCost = totalCost/absoluteQuantity;
		return new Position(ticker, quantity, averageCost, dividends);
	}
	
	
	
}
