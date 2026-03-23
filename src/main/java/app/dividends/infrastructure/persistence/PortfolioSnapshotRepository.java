package app.dividends.infrastructure.persistence;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.dividends.domain.model.PortfolioSnapshot;

@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long>{
	Optional<PortfolioSnapshot> findTopByOrderByLastUpdateDesc();
}
