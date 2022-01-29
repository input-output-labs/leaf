package fr.iolabs.leaf.common.emailing;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

@Service
public class LeafSendgridEmailService {
	private static Logger logger = LoggerFactory.getLogger(LeafSendgridEmailService.class);

	@Value("${sendgrid.api.key}")
	String sendgridAPIKey;

	@Value("${sendgrid.email.from}")
	String sendgridEmailFrom;

	public void sendEmailWithTemplate(String to, String templateId) {
		this.sendEmailWithTemplate(to, templateId, new HashMap<String, String>());
	}

	public void sendEmailWithTemplate(String to, String templateId, Map<String, String> templateData) {
		Email from = new Email(sendgridEmailFrom);
		Email toEmail = new Email(to);
		Content content = new Content("text/html", "<html><body>some text here</body></html>");
		Mail mail = new Mail(from, "Leaf email subject", toEmail, content);
		mail.setTemplateId(templateId);
		if (templateData != null) {
			for (Map.Entry<String, String> data : templateData.entrySet()) {
				mail.personalization.get(0).addDynamicTemplateData(data.getKey(), data.getValue());
			}
		}

		SendGrid sg = new SendGrid(sendgridAPIKey);

		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
			int statusCode = response.getStatusCode();
			if (statusCode > 200 && statusCode < 300) {
				logger.info("Sendgrid email sending success - ID=" + templateId);
			} else {
				logger.warn("Sendgrid email sending failure - ID=" + templateId + " - STATUS_CODE=" + statusCode);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
