package fr.iolabs.leaf.notifications;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface LeafNotificationRepository extends MongoRepository<LeafNotification, String> {

	@Query(value = "{$and: [{'targetAccountId': {$eq: ?0}}, {'channelSendingStatus.UI': {$in: ?1}}]}")
	List<LeafNotification> findUINotificationPerAccountAndStatus(String accoundId, String[] statuses);

}
