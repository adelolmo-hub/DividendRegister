package app.dividends.infrastructure.persistence;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import app.dividends.domain.model.DividendTransaction;
import app.dividends.domain.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>{
	boolean existsByExternalId(String externalId);
	List<Transaction> findAllByOrderByDateAsc();
	List<DividendTransaction> findAllByTicker(String ticker);
	DividendTransaction findByTickerAndDate(String ticker, Date date);
}
