package fr.iolabs.leaf.payment.customer;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.LeafContext;
import fr.iolabs.leaf.authentication.model.LeafAccount;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.common.LeafModuleService;
import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.payment.models.PaymentCustomerModule;

@Service
public class LeafCustomerService {

	@Resource(name = "coreContext")
	private LeafContext coreContext;

	@Autowired
	private LeafModuleService moduleService;

	public PaymentCustomerModule getPaymentCustomerModule(ILeafModular planOwnerTarget) {
		if (planOwnerTarget == null) {
			throw new BadRequestException("No recipiant for payment customer module");
		}
		PaymentCustomerModule paymentCustomerModule = this.moduleService.get(PaymentCustomerModule.class, planOwnerTarget);
		if (paymentCustomerModule.getFreeTrialRemaining() == -1) {
			paymentCustomerModule.setFreeTrialRemaining(1);
		}
		return paymentCustomerModule;
	}

	public PaymentCustomerModule getMyPaymentCustomerModule() {
		LeafAccount account = this.coreContext.getAccount();
		if (account != null) {
			return this.getPaymentCustomerModule(account);
		}
		return null;
	}
}
