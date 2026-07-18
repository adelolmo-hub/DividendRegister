package app.dividends.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.dividends.client.marketdata.model.TickerInformation;

@Repository
public interface TickerInformationRepository extends JpaRepository<TickerInformation, Long>{
	TickerInformation findByTicker(String ticker);
}
