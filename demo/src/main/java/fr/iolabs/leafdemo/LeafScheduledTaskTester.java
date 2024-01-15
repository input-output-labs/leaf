package fr.iolabs.leafdemo;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import fr.iolabs.leaf.taskscheduling.LeafScheduledTaskEvent;
import fr.iolabs.leaf.taskscheduling.LeafScheduledTaskService;

public class LeafScheduledTaskTester {
	@Component
	static class ApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {

		@Autowired
		private LeafScheduledTaskService scheduledTaskService;

		public void onApplicationEvent(final ApplicationReadyEvent event) {
			this.scheduledTaskService.create("ScheduledTaskTest", ZonedDateTime.now().plusMinutes(15));
		}
	}

	@Component
	static class LeafScheduledTaskEventListener implements ApplicationListener<LeafScheduledTaskEvent> {
		public void onApplicationEvent(final LeafScheduledTaskEvent event) {
			System.out.println("Server was started at least 15 minutes ago !!!");
			event.setDone(true);
		}
	}
}
