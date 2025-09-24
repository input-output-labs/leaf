package fr.iolabs.leaf.payment.customer;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.StripeException;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.payment.PaymentModule;

@RestController
@RequestMapping("/api/payment/customer")
public class LeafCustomerController {

	@Resource(name = "coreContext")
	private LeafContext coreContext;
	@Autowired
	private LeafCustomerService customerService;

	@CrossOrigin
	@PostMapping("/paymentmethod")
	public Map<String, String> checkoutPaymentMethod() throws StripeException {
		PaymentModule paymentModule = this.customerService.getMyPaymentModule(true);
		return this.customerService.checkoutPaymentMethod(paymentModule, this.coreContext.getAccount().getId(), "USER");
	}
}
