package fr.iolabs.leaf.notifications;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.LeafContext;

@RestController
@RequestMapping("/api/notifications")
public class LeafNotificationController {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafNotificationRepository notificationRepository;

	@CrossOrigin
	@GetMapping("/mine")
	public List<LeafNotification> listMyNotifications() {
		return this.notificationRepository.findUINotificationPerAccountAndStatus(coreContext.getAccount().getId(),
				new String[] { LeafNotificationChannelSendingStatus.CREATED.toString(),
						LeafNotificationChannelSendingStatus.UI_SEEN.toString() });
	}

	@CrossOrigin
	@PostMapping("/seen")
	public List<LeafNotification> setNotificationsAsSeen(@RequestBody List<String> ids) {
		Iterable<LeafNotification> existingNotifications = this.notificationRepository.findAllById(ids);
		for (LeafNotification existingNotification : existingNotifications) {
			if (coreContext.getAccount().getId().equals(existingNotification.getTargetAccountId())) {
				existingNotification.getChannelSendingStatus().put(LeafNotificationChannel.UI,
						LeafNotificationChannelSendingStatus.UI_SEEN);
			}
		}
		return this.notificationRepository.saveAll(existingNotifications);
	}

}
