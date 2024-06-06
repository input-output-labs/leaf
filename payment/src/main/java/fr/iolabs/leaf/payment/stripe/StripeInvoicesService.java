package fr.iolabs.leaf.payment.stripe;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.param.InvoiceListParams;
import com.stripe.param.InvoiceUpcomingParams;

import fr.iolabs.leaf.payment.models.LeafInvoice;
import fr.iolabs.leaf.payment.models.LeafPrice;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;

@Service
public class StripeInvoicesService {
	private static final long MAX_INVOICES_LIMIT = 10L;

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
