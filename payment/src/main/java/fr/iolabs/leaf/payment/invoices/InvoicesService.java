package fr.iolabs.leaf.payment.invoices;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.common.errors.InternalServerErrorException;
import fr.iolabs.leaf.payment.customer.LeafCustomerService;
import fr.iolabs.leaf.payment.models.LeafInvoice;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.stripe.StripeInvoicesService;

@Service
public class InvoicesService {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private StripeInvoicesService stripeInvoicesService;

	@Autowired
	private LeafCustomerService customerService;

	public List<LeafInvoice> fetchMyInvoices() {
		PaymentCustomerModule customer = this.customerService.getMyPaymentCustomerModule();
		try {
			return this.stripeInvoicesService.getCustomerInvoices(customer);
		} catch (StripeException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Cannot retrieve my invoices");
		}
	}
}
