package app.dividends.domain.model.utils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import app.dividends.domain.model.AbstractTransaction;
import app.dividends.domain.model.Position;

@Component
public class PositionFactory {
	
	public static Position createPosition(
            String ticker, 
            BigDecimal currentAssetValue, 
            BigDecimal exchangeRate, 
            List<AbstractTransaction> transactions) {
        
        if (transactions == null || transactions.isEmpty()) {
            return new Position(ticker, BigDecimal.ZERO, BigDecimal.ZERO); 
        }
        BigDecimal currentAssetValueEur = currentAssetValue.multiply(exchangeRate);
        Position position = new Position(ticker, currentAssetValue, currentAssetValueEur);

        List<AbstractTransaction> sortedTransactions = transactions.stream()
                .sorted(Comparator.comparing(AbstractTransaction::getDate))
                .toList();

        for (AbstractTransaction tx : sortedTransactions) {
            tx.applyTo(position, exchangeRate);
        }

        return position;
    }
}