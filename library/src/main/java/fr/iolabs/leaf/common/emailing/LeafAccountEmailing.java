package fr.iolabs.leaf.common.emailing;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import fr.iolabs.leaf.authentication.model.LeafAccount;

@Service
public class LeafAccountEmailing {

	private static final String MAILGUN = "mailgun";
	private static final String SENDGRID = "sendgrid";

	@Autowired
	private TemplateEngine templateEngine;
	@Autowired
	private LeafMailgunEmailService mailgunEmailService;
	@Autowired
	private LeafSendgridEmailService sendgridEmailService;

	@Value("${leaf.emailing.selected-service}")
	String emailServiceSelection;

	@Value("${leaf.emailing.sendgrid.templates.account-creation}")
	String sendgridAccountCreationTemplateId;

	@Value("${leaf.emailing.sendgrid.templates.password-change-key-sending}")
	String sendgridPasswordChangeKeySendingTemplateId;

	public void sendAccountCreationConfirmation(LeafAccount to) {
		if (MAILGUN.equals(this.emailServiceSelection)) {
			// todo : implement that
		} else if (SENDGRID.equals(this.emailServiceSelection)) {
			this.sendgridEmailService.sendEmailWithTemplate(to.getEmail(), sendgridAccountCreationTemplateId);
		}
	}

	public void sendResetPasswordKey(LeafAccount to, String resetPasswordKey) {
		if (MAILGUN.equals(this.emailServiceSelection)) {
			Context context = new Context();
			context.setVariable("resetPasswordKey", resetPasswordKey);
			String html = templateEngine.process("emailSendPasswordResetKey", context);

			this.mailgunEmailService.sendEmail(to.getEmail(), "Votre clé de re-initialisation de mot de passe.",
					"Clé de re-initialisation : " + resetPasswordKey, html);
		} else if (SENDGRID.equals(this.emailServiceSelection)) {
			Map<String, String> templateData = new HashMap<String, String>();
			templateData.put("passwordChangeKey", resetPasswordKey);

			this.sendgridEmailService.sendEmailWithTemplate(to.getEmail(), sendgridPasswordChangeKeySendingTemplateId,
					templateData);
		}
	}
}
