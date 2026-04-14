package app.dividends.infrastructure.persistence;


import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import app.dividends.domain.model.PortfolioSnapshot;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long>{
	Optional<PortfolioSnapshot> findTopByOrderByLastUpdateDesc();
	@Query("Select p from PortfolioSnapshot p where (ticker, lastUpdate) IN (Select ticker, MAX(lastUpdate) from PortfolioSnapshot group by ticker)")
	List<PortfolioSnapshot> findLastValueStored();
	
}
