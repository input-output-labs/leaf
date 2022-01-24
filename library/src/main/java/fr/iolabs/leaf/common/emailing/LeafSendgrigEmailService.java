package fr.iolabs.leaf.common.emailing;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;

@Service
public class LeafSendgrigEmailService {
	@Value("${sendgrid.api.key}")
    String sendgridAPIKey;
    
    @Value("${sendgrid.email.from}")
    String sendgridEmailFrom;

    public void sendEmailWithTemplate(String to, String templateId) {
		Email from = new Email(sendgridEmailFrom);
		Email toEmail = new Email(to);
		Content content = new Content("text/plain", "Leaf email content");
		Mail mail = new Mail(from, "Leaf email subject", toEmail, content);
		mail.setTemplateId(templateId);

		SendGrid sg = new SendGrid(sendgridAPIKey);

		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());
			Response response = sg.api(request);
			if (response.getStatusCode() > 200 && response.getStatusCode() < 300) {
				System.out.println("Confirmation email sent to user");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
    }
}
