package app.dividends.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.dividends.domain.model.TickerInformation;

@Repository
public interface TickerInformationRepository extends JpaRepository<TickerInformation, Long>{
	TickerInformation findByTicker(String ticker);
}
