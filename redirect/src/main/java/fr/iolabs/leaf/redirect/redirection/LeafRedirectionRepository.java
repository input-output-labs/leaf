package fr.iolabs.leaf.redirect.redirection;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeafRedirectionRepository extends MongoRepository<LeafRedirection, Long> {
	List<LeafRedirection> findAllByCreationBatchId(String creationBatchId);
}
