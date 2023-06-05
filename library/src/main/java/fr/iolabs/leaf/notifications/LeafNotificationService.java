package fr.iolabs.leaf.notifications;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.emailing.LeafSendgridEmailService;

@Service
public class LeafNotificationService {

	@Autowired
	private LeafNotificationRepository notificationRepository;
	@Autowired
	private LeafNotificationConfig notificationConfig;
	@Autowired
	private LeafSendgridEmailService leafSendgridEmailService;
	@Autowired
	private LeafAccountRepository leafAccountRepository;
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	public void emit(LeafNotification notification) {
		Map<String, String> notificationConfig = this.notificationConfig.getConfigByCode(notification.getCode());

		Optional<LeafAccount> optTargetAccount = this.leafAccountRepository.findById(notification.getTargetAccountId());
		if (optTargetAccount.isPresent()) {
			for (LeafNotificationChannel channel : LeafNotificationChannel.all()) {
				String channelConfig = notificationConfig != null
						? notificationConfig.get(channel.toString().toLowerCase())
						: null;
				this.emitOn(channel, notification, channelConfig, optTargetAccount.get());
			}
			this.notificationRepository.save(notification);
		} else {
			System.err.println("[LeafNotificationService] Cannot retrieve targetted account with ID="
					+ notification.getTargetAccountId());
		}
	}

	private void emitOn(LeafNotificationChannel channel, LeafNotification notification, String config,
			LeafAccount targetAccount) {
		if (config == null) {
			notification.getChannelSendingStatus().put(channel, LeafNotificationChannelSendingStatus.SKIP_NO_CONFIG);
			return;
		}
		if ("skip".equalsIgnoreCase(config)) {
			notification.getChannelSendingStatus().put(channel, LeafNotificationChannelSendingStatus.SKIP_PER_CONFIG);
			return;
		}
		switch (channel) {
		case UI:
			this.emitOnUI(notification);
			break;
		case EMAIL:
			this.emitOnEmail(notification, config, targetAccount);
			break;
		case WS:
			this.emitOnWebsocket(notification, config, targetAccount);
			break;
		}
	}

	private void emitOnUI(LeafNotification notification) {
		// Nothing to do
	}

	private void emitOnEmail(LeafNotification notification, String config, LeafAccount targetAccount) {
		String targetAccountEmail = targetAccount.getEmail();
		this.leafSendgridEmailService.sendEmailWithTemplate(targetAccountEmail, config, notification.getPayload());
		notification.getChannelSendingStatus().put(LeafNotificationChannel.EMAIL,
				LeafNotificationChannelSendingStatus.EMAIL_SENT);
	}

	private void emitOnWebsocket(LeafNotification notification, String config, LeafAccount targetAccount) {
		simpMessagingTemplate.convertAndSend("/notifications/" + notification.getTargetAccountId(), "REFRESH");
		notification.getChannelSendingStatus().put(LeafNotificationChannel.WS,
				LeafNotificationChannelSendingStatus.WS_SENT);
	}
}
