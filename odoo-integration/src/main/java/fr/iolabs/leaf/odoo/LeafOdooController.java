package fr.iolabs.leaf.odoo;

import fr.iolabs.leaf.odoo.contact.OdooContact;
import fr.iolabs.leaf.odoo.contact.OdooContactService;
import fr.iolabs.leaf.odoo.opportunity.OdooOpportunity;
import fr.iolabs.leaf.odoo.opportunity.OdooOpportunityService;
import fr.iolabs.leaf.odoo.invoice.OdooInvoice;
import fr.iolabs.leaf.odoo.invoice.OdooInvoiceService;
import fr.iolabs.leaf.odoo.quote.OdooQuote;
import fr.iolabs.leaf.odoo.quote.OdooQuoteService;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/odoo")
public class LeafOdooController {

	@Autowired
	private OdooCredentialsResolver odooCredentialsResolver;

	@Autowired
	private OdooContactService odooContactService;

	@Autowired
	private OdooOpportunityService odooOpportunityService;

	@Autowired
	private OdooQuoteService odooQuoteService;

	@Autowired
	private OdooInvoiceService odooInvoiceService;

	@CrossOrigin
	@GetMapping("/contacts")
	public List<OdooContact> listContacts(@RequestParam(name = "limit", required = false) Integer limit) {
		OdooCredentials credentials = this.odooCredentialsResolver.resolveForApi();
		return this.odooContactService.listContacts(credentials, limit);
	}

	@CrossOrigin
	@GetMapping("/opportunities")
	public List<OdooOpportunity> listOpportunities(
		@RequestParam(name = "limit", required = false) Integer limit,
		@RequestParam(name = "offset", required = false) Integer offset
	) {
		OdooCredentials credentials = this.odooCredentialsResolver.resolveForApi();
		if (offset != null) {
			return this.odooOpportunityService.listOpportunitiesPage(credentials, limit, offset);
		}
		return this.odooOpportunityService.listOpportunities(credentials, limit);
	}

	@CrossOrigin
	@GetMapping("/quotes/signed")
	public List<OdooQuote> listQuotesSignedBetween(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to
	) {
		OdooCredentials credentials = this.odooCredentialsResolver.resolveForApi();
		return this.odooQuoteService.listQuotesSignedBetween(credentials, from, to);
	}

	@CrossOrigin
	@GetMapping("/invoices/paid")
	public List<OdooInvoice> listInvoicesPaidBetween(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to
	) {
		OdooCredentials credentials = this.odooCredentialsResolver.resolveForApi();
		return this.odooInvoiceService.listInvoicesPaidBetween(credentials, from, to);
	}

	@CrossOrigin
	@GetMapping("/invoices/unpaid")
	public List<OdooInvoice> listInvoicesUnpaidDuringPeriod(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to
	) {
		OdooCredentials credentials = this.odooCredentialsResolver.resolveForApi();
		return this.odooInvoiceService.listInvoicesUnpaidDuringPeriod(credentials, from, to);
	}
}
