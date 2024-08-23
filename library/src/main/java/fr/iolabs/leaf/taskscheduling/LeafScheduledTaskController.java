package fr.iolabs.leaf.taskscheduling;

import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.annotations.AdminOnly;
import fr.iolabs.leaf.common.errors.NotFoundException;

@RestController
@RequestMapping("/api/tasks")
public class LeafScheduledTaskController {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafScheduledTaskRepository scheduledTaskRepository;

	@Autowired
	private LeafScheduledTaskService scheduledTaskService;
	
	@CrossOrigin
	@AdminOnly
	@GetMapping("/{id}")
	public LeafScheduledTask findTaskById(@PathVariable String id) {
		Optional<LeafScheduledTask> optTask = this.scheduledTaskRepository.findById(id);
		if (optTask.isEmpty()) {
			throw new NotFoundException("No task with id " + id);
		}
		return optTask.get();
	}

	@CrossOrigin
	@AdminOnly
	@GetMapping("/{id}/execute")
	public void executeTaskById(@PathVariable String id) {
		Optional<LeafScheduledTask> optTask = this.scheduledTaskRepository.findById(id);
		if (optTask.isEmpty()) {
			throw new NotFoundException("No task with id " + id);
		}
		this.scheduledTaskService.executeTask(optTask.get());
	}

	@CrossOrigin
	@AdminOnly
	@GetMapping("/findAndExecute")
	public void triggerAll(@PathVariable String id) {
		this.scheduledTaskService.findAndExecuteScheduledTasks();
	}

}
