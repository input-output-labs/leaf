package fr.iolabs.leaf.common.sms;

import fr.iolabs.leaf.common.sms.model.LeafSmsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeafSmsTemplateRepository extends MongoRepository<LeafSmsTemplate, String> {
}
