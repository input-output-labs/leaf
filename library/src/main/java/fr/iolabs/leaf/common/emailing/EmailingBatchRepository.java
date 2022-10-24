package fr.iolabs.leaf.common.emailing;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import fr.iolabs.leaf.common.emailing.models.EmailingBatch;

@Repository
public interface EmailingBatchRepository extends MongoRepository<EmailingBatch, String> {
	public List<EmailingBatch> findByFinishedFalse();
}
