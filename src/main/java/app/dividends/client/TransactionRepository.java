package app.dividends.client;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import app.dividends.service.models.AbstractTransaction;
import app.dividends.service.models.DividendTransaction;

@Repository
public interface TransactionRepository extends JpaRepository<AbstractTransaction, Long>{
	boolean existsByExternalId(String externalId);
	List<AbstractTransaction> findAllByOrderByDateAsc();
	List<DividendTransaction> findAllByTicker(String ticker);

	@Query("SELECT d FROM DividendTransaction d WHERE d.ticker = :ticker AND d.date = :date")
	Optional<DividendTransaction> findDividendByTickerAndDate(@Param("ticker") String ticker, @Param("date") Date date);
}
