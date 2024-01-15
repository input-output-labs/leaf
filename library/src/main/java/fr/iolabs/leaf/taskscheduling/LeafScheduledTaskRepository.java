package fr.iolabs.leaf.taskscheduling;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface LeafScheduledTaskRepository extends MongoRepository<LeafScheduledTask, String> {
/*
	@Query(value = "{$and: [{'targetAccountId': {$eq: ?0}}, {'channelSendingStatus.UI': {$in: ?1}}]}")
	List<LeafNotification> findUINotificationPerAccountAndStatus(String accoundId, String[] statuses);
*/
}
