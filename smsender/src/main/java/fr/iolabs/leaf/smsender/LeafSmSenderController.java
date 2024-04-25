package fr.iolabs.leaf.smsender;

import fr.iolabs.leaf.smsender.models.SmsRequest;
import fr.iolabs.leaf.smsender.services.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling SMS sending requests.
 */
@RestController
@RequestMapping("/api/smsender")
public class LeafSmSenderController {
	private final Logger logger = LoggerFactory.getLogger(LeafSmSenderController.class);

	@Autowired
	private SmsService smsService;

	/**
	 * Endpoint for sending SMS.
	 *
	 * @param smsRequest contains the recipient and message.
	 * @return HTTP response indicating success or failure of the execution.
	 */
	@CrossOrigin
	@PostMapping("/send-sms")
	public HttpEntity<String> sendSMS(@RequestBody SmsRequest smsRequest) {
		if (smsRequest == null || smsRequest.getTo() == null || smsRequest.getMessage() == null) {
			return ResponseEntity.badRequest().body("Invalid SMS request. Missing required fields.");
		}
		try {
			this.smsService.sendSMS(smsRequest.getTo(), smsRequest.getMessage());
			return ResponseEntity.ok("sent successfully");
		} catch (Exception e) {
			logger.error("Failed to send SMS to {}}", smsRequest.getTo());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send the sms");
		}
	}
}