package app.dividends.infrastructure.persistence;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.dividends.domain.model.DividendTransaction;
import app.dividends.domain.model.AbstractTransaction;

@Repository
public interface TransactionRepository extends JpaRepository<AbstractTransaction, Long>{
	boolean existsByExternalId(String externalId);
	List<AbstractTransaction> findAllByOrderByDateAsc();
	List<DividendTransaction> findAllByTicker(String ticker);
	DividendTransaction findByTickerAndDate(String ticker, Date date);
}
