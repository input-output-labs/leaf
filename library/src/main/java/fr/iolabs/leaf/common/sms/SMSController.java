package fr.iolabs.leaf.common.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
public class SMSController {

	@Autowired
	private SMSService smsService;
	
	@CrossOrigin
	@GetMapping("/send")
	public boolean sendSms() {
		System.out.println("YES SMS");
		return this.smsService.sendSMS();
	}
}
