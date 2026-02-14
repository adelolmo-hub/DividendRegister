package app.dividends.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import app.dividends.domain.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>{
	boolean existsByExternalId(String externalId);
	
}
