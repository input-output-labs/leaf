package fr.iolabs.leaf.payment.models;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeafPaymentTransactionRepository extends MongoRepository<LeafPaymentTransaction, String> {}
