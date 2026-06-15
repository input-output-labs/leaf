package fr.iolabs.leaf.odoo;

import fr.iolabs.leaf.odoo.contact.OdooContact;
import fr.iolabs.leaf.odoo.contact.OdooContactService;
import fr.iolabs.leaf.odoo.opportunity.OdooOpportunity;
import fr.iolabs.leaf.odoo.opportunity.OdooOpportunityService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
}
