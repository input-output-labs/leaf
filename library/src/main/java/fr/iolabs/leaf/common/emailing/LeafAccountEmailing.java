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

	public void sendResetPasswordKey(LeafAccount to, String resetPasswordKey) {
		Map<String, Object> templateData = new HashMap<String, Object>();
		templateData.put("passwordChangeKey", resetPasswordKey);

		this.sendgridEmailService.sendEmailWithTemplate(to.getEmail(), sendgridPasswordChangeKeySendingTemplateId,
				templateData);
	}
}
