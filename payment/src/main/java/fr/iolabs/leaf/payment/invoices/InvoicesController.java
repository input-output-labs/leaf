package fr.iolabs.leaf.payment.invoices;

import java.util.List;

import javax.annotation.security.PermitAll;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.payment.models.LeafInvoice;
import fr.iolabs.leaf.payment.plan.PlanService;

@RestController
@RequestMapping("/api/payment/invoices")
public class InvoicesController {
	@Autowired
	private PlanService planService;
	
	@PermitAll
	@CrossOrigin
	@GetMapping
	public List<LeafInvoice> fetchInvoices(@RequestParam String type) {
		switch(type) {
			case "plan":
				return this.planService.fetchInvoices();
			default:
				throw new BadRequestException("Invalid invoices type: " + type);
		}
	}
}
