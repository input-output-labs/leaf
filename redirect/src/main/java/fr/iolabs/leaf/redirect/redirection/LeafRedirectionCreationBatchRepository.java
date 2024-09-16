package fr.iolabs.leaf.redirect.redirection;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeafRedirectionCreationBatchRepository extends MongoRepository<LeafRedirectionCreationBatch, String> {
	LeafRedirectionCreationBatch findFirstByOrderByEndAdDesc();
}
