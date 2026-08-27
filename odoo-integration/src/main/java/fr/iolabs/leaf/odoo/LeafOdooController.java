package fr.iolabs.leaf.odoo;

import fr.iolabs.leaf.odoo.contact.OdooContact;
import fr.iolabs.leaf.odoo.contact.OdooContactService;
import fr.iolabs.leaf.odoo.opportunity.OdooOpportunity;
import fr.iolabs.leaf.odoo.opportunity.OdooOpportunityService;
import fr.iolabs.leaf.odoo.invoice.OdooInvoice;
import fr.iolabs.leaf.odoo.invoice.OdooInvoiceService;
import fr.iolabs.leaf.odoo.product.OdooProduct;
import fr.iolabs.leaf.odoo.product.OdooProductQueryService;
import fr.iolabs.leaf.odoo.quote.OdooCreateQuoteRequest;
import fr.iolabs.leaf.odoo.quote.OdooQuote;
import fr.iolabs.leaf.odoo.quote.OdooQuoteService;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

	@Autowired
	private OdooProductQueryService odooProductQueryService;

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
	@GetMapping("/products")
	public List<OdooProduct> listProducts(@RequestParam(name = "limit", required = false) Integer limit) {
		OdooCredentials credentials = this.odooCredentialsResolver.resolveForApi();
		return this.odooProductQueryService.listProducts(credentials, limit);
	}

	@CrossOrigin
	@PostMapping("/quotes")
	public OdooQuote createQuote(@RequestBody OdooCreateQuoteRequest request) {
		OdooCredentials credentials = this.odooCredentialsResolver.resolveForApi();
		return this.odooQuoteService.createQuote(credentials, request);
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
	@GetMapping("/quotes/{id}")
	public OdooQuote getQuoteById(@PathVariable("id") int id) {
		OdooCredentials credentials = this.odooCredentialsResolver.resolveForApi();
		return this.odooQuoteService.getQuoteById(credentials, id);
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
