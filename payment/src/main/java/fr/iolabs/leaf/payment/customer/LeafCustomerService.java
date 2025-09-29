package fr.iolabs.leaf.payment.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.checkout.Session;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.LeafAccountRepository;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.common.LeafModuleService;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.organization.LeafOrganizationRepository;
import fr.iolabs.leaf.organization.model.LeafOrganization;
import fr.iolabs.leaf.payment.PaymentModule;
import fr.iolabs.leaf.payment.models.PaymentMethod;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;

@Service
public class LeafCustomerService {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafModuleService moduleService;

	@Value("${leaf.appDomain}")
	String protocol_hostname;

	@Autowired
	private LeafPaymentConfig paymentConfig;

	@Autowired
	private LeafAccountRepository accountRepository;

	@Autowired
	private LeafOrganizationRepository organizationRepository;

	public PaymentModule getPaymentModule(ILeafModular planOwnerTarget) {
		if (planOwnerTarget == null) {
			throw new BadRequestException("No recipiant for payment customer module");
		}
		PaymentModule paymentModule = this.moduleService.get(PaymentModule.class, planOwnerTarget);
		if (paymentModule.getFreeTrialRemaining() == -1) {
			paymentModule.setFreeTrialRemaining(1);
		}
		return paymentModule;
	}

	public PaymentModule getMyPaymentModule() {
		return this.getMyPaymentModule(false);
	}

	public PaymentModule getMyPaymentModule(boolean checkStripeCustomer) {
		LeafAccount account = this.coreContext.getAccount();
		if (account != null) {
			PaymentModule paymentModule = this.getPaymentModule(account);
			if (paymentModule.getStripeCustomerId() == null || paymentModule.getStripeCustomerId().isBlank()) {
				try {
					this.checkStripeCustomer(paymentModule, account.getEmail());
					this.coreContext.setAccount(this.accountRepository.save(account));
				} catch (StripeException e) {
					e.printStackTrace();
				}
			}
			return paymentModule;
		}
		return null;
	}

	public PaymentModule getMyOrganizationPaymentModule() {
		LeafOrganization organization = this.coreContext.getOrganization();
		if (organization != null) {
			PaymentModule paymentModule = this.getPaymentModule(organization);
			if (paymentModule.getStripeCustomerId() == null || paymentModule.getStripeCustomerId().isBlank()) {
				try {
					this.checkStripeCustomer(paymentModule);
					this.coreContext.setOrganization(this.organizationRepository.save(organization));
				} catch (StripeException e) {
					e.printStackTrace();
				}
			}
			return paymentModule;
		}
		return null;
	}

	public Customer checkStripeCustomer(PaymentModule paymentModule) throws StripeException {
		return this.checkStripeCustomer(paymentModule, null);
	}

	public Customer checkStripeCustomer(PaymentModule paymentModule, String email) throws StripeException {
		// Customer
		if (paymentModule.getStripeCustomerId() != null) {
			// if here : verify it
			return Customer.retrieve(paymentModule.getStripeCustomerId());
		} else {
			// if missing, create it
			Map<String, Object> creationParams = new HashMap<>();
			if (email != null) {
				creationParams.put("email", email);
			}
			Customer stripeCustomer = Customer.create(creationParams);
			paymentModule.setStripeCustomerId(stripeCustomer.getId());
			return stripeCustomer;
		}
	}


	public Map<String, String> checkoutPaymentMethod(PaymentModule paymentModule, String iModularId, String innerType)
			throws StripeException {
		// Verify customer
		Customer.retrieve(paymentModule.getStripeCustomerId());

		// Following instruction from:
		// https://stripe.com/docs/payments/checkout/subscriptions/update-payment-details#retrieve-checkout-session

		// Create checkout session
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("goal", "setupCustomerDefaultPaymentMethod");
		metadata.put("innerId", iModularId);
		metadata.put("innerType", innerType);
		metadata.put("customer_id", paymentModule.getStripeCustomerId());
		Map<String, Object> setup_intent_data = new HashMap<>();
		setup_intent_data.put("metadata", metadata);

		Map<String, Object> params = new HashMap<>();
		params.put("success_url", this.protocol_hostname + paymentConfig.getRedirect().get("checkout_success"));
		params.put("cancel_url", this.protocol_hostname + paymentConfig.getRedirect().get("checkout_cancel"));
		params.put("payment_method_types", List.of("card"));

		params.put("mode", "setup");
		params.put("setup_intent_data", setup_intent_data);

		params.put("customer", paymentModule.getStripeCustomerId());

		Session session = Session.create(params);

		Map<String, String> checkoutData = new HashMap<>();
		checkoutData.put("checkout_url", session.getUrl());
		return checkoutData;
	}


	public ILeafModular getLeafModular(String innerType, String iModularId) {
		ILeafModular iModular = null;
		switch (innerType) {
		case "USER":
			Optional<LeafAccount> opt = this.accountRepository.findById(iModularId);
			if (opt.isPresent()) {
				iModular = opt.get();
			}
			break;
		case "ORGANIZATION":
			Optional<LeafOrganization> optOrg = this.organizationRepository.findById(iModularId);
			if (optOrg.isPresent()) {
				iModular = optOrg.get();
			}
			break;
		default:
			break;
		}
		return iModular;
	}

	private ILeafModular saveLeafModular(ILeafModular iModular) {
		if (iModular instanceof LeafAccount) {
			return this.accountRepository.save((LeafAccount) iModular);
		} else if (iModular instanceof LeafOrganization) {
			return this.organizationRepository.save((LeafOrganization) iModular);
		}
		return null;
	}

	public void setCustomerPaymentMethod(String innerType, String iModularId, PaymentMethod pm) {
		ILeafModular iModular = this.getLeafModular(innerType, iModularId);
		PaymentModule payment = this.getPaymentModule(iModular);
		payment.setDefaultPaymentMethod(pm);
		payment.getMetadata().updateLastModification();
		this.saveLeafModular(iModular);
	}
}
