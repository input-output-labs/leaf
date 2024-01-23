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
import com.mashape.unirest.http.utils.URLParamEncoder;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

@Service
public class LeafSendgridEmailService {
	private static Logger logger = LoggerFactory.getLogger(LeafSendgridEmailService.class);

	@Value("${leaf.emailing.sendgrid.api.key}")
	String sendgridAPIKey;

	@Value("${leaf.emailing.sendgrid.email.from}")
	String sendgridEmailFrom;

	@Value("${leaf.appDomain}")
	String appDomain;

	@Value("${leaf.emailing.unsubscribe-url}")
	String emailingUnsubscribeUrl;

	public String sendEmailWithTemplate(String to, String templateId) {
		return this.sendEmailWithTemplate(to, templateId, new HashMap<String, Object>());
	}

	public String sendEmailWithTemplate(String to, String templateId, String emailingCategoryName) {
		return this.sendEmailWithTemplate(to, templateId, new HashMap<String, Object>(), emailingCategoryName);
	}

	public String sendEmailWithTemplate(String to, String templateId, Map<String, Object> templateData) {
		return this.sendEmailWithTemplate(to, templateId, templateData, null);
	}

	public String sendEmailWithTemplate(String to, String templateId, Map<String, Object> templateData,
			String emailingCategoryName) {
		Email from = new Email(sendgridEmailFrom);
		Email toEmail = new Email(to);
		Content content = new Content("text/html", "<html><body>some text here</body></html>");
		Mail mail = new Mail(from, "Leaf email subject", toEmail, content);
		mail.setTemplateId(templateId);
		if (templateData != null) {
			for (Map.Entry<String, Object> data : templateData.entrySet()) {
				mail.personalization.get(0).addDynamicTemplateData(data.getKey(), data.getValue());
			}
			if (emailingCategoryName != null && this.emailingUnsubscribeUrl != null) {
				String encodedType = URLParamEncoder.encode(emailingCategoryName);
				String encodedEmail = URLParamEncoder.encode(to);

				String url = this.appDomain + "/"+ this.emailingUnsubscribeUrl + "?type=" + encodedType
						+ "&email=" + encodedEmail;
				String protocol;
				String hostAndPath;
				if (url.split("://").length > 1) {
					protocol = url.split("://")[0];
					hostAndPath = url.split("://")[1];
				} else {
					protocol = "https";
					hostAndPath = url.split("://")[0];
				}
				while(hostAndPath.contains("//")) {
					hostAndPath = hostAndPath.replace("//", "/");
				}
				url = protocol + "://" + hostAndPath;
				mail.personalization.get(0).addDynamicTemplateData("unsubscribeUrl", url);
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
			return ex.getMessage();
		}
		return "";
	}
}
