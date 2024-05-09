package fr.iolabs.leaf.common.emailing;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.authentication.model.LeafAccount;

@Service
public class LeafAccountEmailing {
	@Autowired
	private LeafSendgridEmailService sendgridEmailService;

	@Value("${leaf.emailing.sendgrid.templates.password-change-key-sending}")
	String sendgridPasswordChangeKeySendingTemplateId;

	@Value("${leaf.emailing.sendgrid.templates.password-change-key-sending-temporary-account}")
	String sendgridPasswordChangeKeySendingTemporaryAccountTemplateId;

	public void sendResetPasswordKey(LeafAccount to, String resetPasswordKey, boolean wasTemporaryAccount) {
		Map<String, Object> templateData = new HashMap<String, Object>();
		templateData.put("passwordChangeKey", resetPasswordKey);
		String templateId = sendgridPasswordChangeKeySendingTemplateId;
		if(wasTemporaryAccount) {
			templateId = sendgridPasswordChangeKeySendingTemporaryAccountTemplateId;
		}
		this.sendgridEmailService.sendEmailWithTemplate(to.getEmail(), templateId,
				templateData);
	}
}
