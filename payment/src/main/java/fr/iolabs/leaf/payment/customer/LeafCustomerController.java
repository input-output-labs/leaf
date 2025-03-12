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
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;

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
		PaymentCustomerModule customerModule = this.customerService.getMyPaymentCustomerModule(true);
		return this.customerService.checkoutPaymentMethod(customerModule, this.coreContext.getAccount().getId(), "USER");
	}
}
