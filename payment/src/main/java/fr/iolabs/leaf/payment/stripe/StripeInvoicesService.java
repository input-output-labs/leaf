package fr.iolabs.leaf.payment.stripe;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceItem;
import com.stripe.param.InvoiceCreateParams;
import com.stripe.param.InvoiceItemCreateParams;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.InvoicePayParams;
import com.stripe.param.InvoiceUpcomingParams;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.payment.customer.LeafCustomerService;
import fr.iolabs.leaf.payment.models.LeafInvoice;
import fr.iolabs.leaf.payment.models.LeafPrice;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;
import fr.iolabs.leaf.payment.stripe.models.InvoiceCreationAction;
import fr.iolabs.leaf.payment.stripe.models.InvoiceItemCreationAction;

@Service
public class StripeInvoicesService {
	private static final long MAX_INVOICES_LIMIT = 10L;
	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafCustomerService customerService;

	public List<LeafInvoice> getCustomerInvoices(PaymentCustomerModule customer) throws StripeException {
		InvoiceUpcomingParams upcomingInvoiceParams = InvoiceUpcomingParams.builder()
				.setCustomer(customer.getStripeId()).build();
		Invoice upcomingInvoice = null;
		try {
			upcomingInvoice = Invoice.upcoming(upcomingInvoiceParams);
		} catch (StripeException e) {

		}

		InvoiceListParams invoicesParams = InvoiceListParams.builder().setCustomer(customer.getStripeId())
				.setLimit(MAX_INVOICES_LIMIT).build();
		List<Invoice> invoices = Invoice.list(invoicesParams).getData();

		List<LeafInvoice> leafInvoices = new ArrayList<>();
		if (upcomingInvoice != null) {
			LeafInvoice lInvoice = this.stripeToLeafInvoice(upcomingInvoice);
			lInvoice.setIncoming(true);
			leafInvoices.add(lInvoice);
		}
		if (invoices != null) {
			for (Invoice invoice : invoices) {
				leafInvoices.add(this.stripeToLeafInvoice(invoice));
			}
		}

		return leafInvoices;
	}

	public Invoice generateInvoice(InvoiceCreationAction action) throws StripeException {
		// RetrieveCustomer
		LeafAccount myAccount = this.coreContext.getAccount();
		return this.generateInvoiceForAccount(action, myAccount);
	}

	public Invoice generateInvoiceForAccount(InvoiceCreationAction action, LeafAccount account) throws StripeException {
		// RetrieveCustomer
		PaymentCustomerModule customer = this.customerService.getPaymentCustomerModule(account);
		Customer stripeCustomer = this.customerService.checkStripeCustomer(customer, account.getEmail());
		return this.generateInvoice(stripeCustomer.getId(), action);
	}

	public Invoice generateInvoice(String customerId, InvoiceCreationAction action) throws StripeException {
		// RetrieveCustomer
		InvoiceCreateParams invoiceParams = InvoiceCreateParams.builder().setCustomer(customerId).setDescription(action.getDescription()).setAutoAdvance(action.isAutoAdvance())
				.build();
		Invoice invoice = Invoice.create(invoiceParams);
		
		for (InvoiceItemCreationAction itemAction : action.getItems()) {
			InvoiceItemCreateParams invoiceItemParams = InvoiceItemCreateParams.builder().setCustomer(customerId)
					.setInvoice(invoice.getId())
					.setAmount(itemAction.getAmount())
					.setCurrency(itemAction.getCurrency())
					.setDescription(itemAction.getDescription()).build();
			InvoiceItem.create(invoiceItemParams);
		}
		
		Invoice updatedInvoice = Invoice.retrieve(invoice.getId());
		InvoicePayParams invoicePayParams = InvoicePayParams.builder().setPaidOutOfBand(true).build();
		Invoice paidInvoice = updatedInvoice.pay(invoicePayParams);
		return paidInvoice;
	}

	public LeafInvoice stripeToLeafInvoice(Invoice invoice) {
		LeafInvoice lInvoice = new LeafInvoice();
		LeafPrice price = new LeafPrice();
		price.setAmount(invoice.getTotal());
		price.setCurrency(invoice.getCurrency());

		lInvoice.setPrice(price);
		lInvoice.setStatus(invoice.getStatus());
		lInvoice.setCreationDate(new Timestamp(invoice.getCreated() * 1000).toLocalDateTime().atZone(ZoneId.of("UTC")));
		lInvoice.setPdfUrl(invoice.getInvoicePdf());

		return lInvoice;
	}
}
