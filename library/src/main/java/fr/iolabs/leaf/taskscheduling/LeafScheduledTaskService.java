package fr.iolabs.leaf.taskscheduling;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mongodb.client.result.UpdateResult;

@Service
public class LeafScheduledTaskService {
	private static final int SCHEDULED_DELAY = 15;

	@Autowired
	private LeafScheduledTaskRepository scheduledTaskRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void create(String type, ZonedDateTime executeAt) {
		this.create(type, executeAt, null);
	}

	public void create(String type, ZonedDateTime executeAt, Map<String, Object> payload) {
		if (payload == null) {
			payload = new HashMap<>();
		}
		LeafScheduledTask task = new LeafScheduledTask();
		task.setType(type);
		task.setExecuteAt(executeAt);
		task.setPayload(payload);

		this.scheduledTaskRepository.insert(task);
	}

	@Scheduled(fixedDelay = SCHEDULED_DELAY, timeUnit = TimeUnit.MINUTES)
	public void findAndExecuteScheduledTasks() {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime unlockTime = ZonedDateTime.now().plusSeconds(SCHEDULED_DELAY);

		Query searchQuery = new Query();
		searchQuery.addCriteria(Criteria.where("doneAt").isNull());
		searchQuery.addCriteria(new Criteria().orOperator(Criteria.where("lockedAt").isNull(),
				Criteria.where("lockedAt").lt(unlockTime)));
		searchQuery.addCriteria(Criteria.where("executeAt").lt(now));
		List<LeafScheduledTask> tasks = this.mongoTemplate.find(searchQuery, LeafScheduledTask.class);
		System.out.println("Scheduled tasks found : " + tasks.size());

		for (LeafScheduledTask task : tasks) {
			System.out.print("[LeafScheduledTask] " + task.getId() + " (" + task.getType() + ") lockedAt="
					+ task.getLockedAt() + "; done=" + task.getDoneAt());

			// Second control, in addition to DB query
			boolean isUnlocked = task.getLockedAt() == null || task.getLockedAt().isBefore(unlockTime);
			boolean isNotDone = task.getDoneAt() == null;
			boolean shouldExecute = task.getExecuteAt() == null || task.getExecuteAt().isBefore(now);
			if (shouldExecute && isNotDone && isUnlocked) {
				System.out.println(" - to be executed");
				Query query = new Query();
				query.addCriteria(Criteria.where("id").is(task.getId()));
				query.addCriteria(new Criteria().orOperator(Criteria.where("lockedAt").isNull(),
						Criteria.where("lockedAt").lt(unlockTime)));
				Update update = new Update();
				task.setLockedAt(now);
				update.set("lockedAt", now);
				UpdateResult updateResult = this.mongoTemplate.updateFirst(query, update, LeafScheduledTask.class);

				System.out.println("Match count : " + updateResult.getMatchedCount());
				System.out.println("Modified count : " + updateResult.getModifiedCount());

				/**
				 * If query don't find or update document it means that another instance of the
				 * server has already locked the tasks. This is assuming the database does not
				 * allow 2 query to be executed at the same time.
				 */
				if (updateResult.getMatchedCount() == 1 && updateResult.getModifiedCount() == 1) {
					this.executeTask(task);
				}
			} else {
				System.out.println(" - to ignore");
			}
		}
	}

	public void executeTask(LeafScheduledTask task) {
		LeafScheduledTaskEvent event = new LeafScheduledTaskEvent(this, task.getType(), task.getPayload());
		this.applicationEventPublisher.publishEvent(event);

		if (event.isDone()) {
			task.setDoneAt(ZonedDateTime.now());
			this.scheduledTaskRepository.save(task);
		} else {
			System.err.println("/!\\ Task cannot be done /!\\");
		}
	}
}
