package fr.iolabs.leaf.payment.models;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LeafPaymentTransactionRepository extends MongoRepository<LeafPaymentTransaction, String> {
	@Query("{ checkoutSessionId : ?0 }")
	public Optional<LeafPaymentTransaction> findByCheckoutSessionId(String checkoutSessionId);
}
