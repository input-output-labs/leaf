package fr.iolabs.leaf.payment.stripe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Price;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionItem;
import com.stripe.model.TaxId;
import com.stripe.model.UsageRecord;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.SubscriptionUpdateParams.CollectionMethod;
import com.stripe.param.TaxIdCollectionCreateParams;
import com.stripe.param.UsageRecordCreateOnSubscriptionItemParams;

import fr.iolabs.leaf.authentication.model.profile.LeafAccountProfile;
import fr.iolabs.leaf.common.ILeafModular;
import fr.iolabs.leaf.payment.PaymentModule;
import fr.iolabs.leaf.payment.PaymentModule.ExtraServicePrice;
import fr.iolabs.leaf.payment.customer.LeafCustomerService;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.models.LeafPaymentPlan;
import fr.iolabs.leaf.payment.services.LeafServiceSubscriptionSynchronization.LeafServiceSubscriptionSynchronizationAction;

@Service
public class StripeSubcriptionService implements InitializingBean {

	@Value("${leaf.payment.stripe.api.key}")
	private String privateKey;

	@Autowired
	private LeafCustomerService customerService;

	@Autowired
	private LeafPaymentConfig paymentConfig;
	
	@Override
	public void afterPropertiesSet() {
		Stripe.apiKey = this.privateKey;
	}

	public void createSubscription(ILeafModular iModular, PaymentModule paymentModule, LeafPaymentPlan newPlan, LeafPaymentPlan previousPlan)
			throws StripeException {

		this.customerService.checkStripeCustomer(paymentModule);
		
		String existingSubscriptionId = null;
		if (previousPlan != null && previousPlan.getStripeSubscriptionId() != null) {
			existingSubscriptionId = previousPlan.getStripeSubscriptionId();
		}
		
		Subscription stripeSubscription = null;
		if (existingSubscriptionId != null) {
			// Update
			stripeSubscription = updateSubscription(paymentModule, existingSubscriptionId, newPlan, previousPlan);
		}
		if (existingSubscriptionId == null) {
			// Create
			stripeSubscription = createSubscription(iModular.getId(), paymentModule.getStripeCustomerId(), newPlan, paymentModule.getFreeTrialRemaining() > 0);
		}

		// Subscription
		newPlan.setStripeSubscriptionId(stripeSubscription.getId());
		newPlan.setSuspended(false);
		paymentModule.decreaseFreeTrialRemaining();
	}

	private Subscription createSubscription(String innerId, String stripeCustomerId, LeafPaymentPlan plan, boolean trialAllowed)
			throws StripeException {
		Map<String, Object> params = new HashMap<>();
		params.put("customer", stripeCustomerId);

		List<Object> items = new ArrayList<>();
		Map<String, Object> item1 = new HashMap<>();
		item1.put("price", plan.getStripePriceId());
		items.add(item1);
		params.put("items", items);

		Map<String, Object> metadata = new HashMap<>();
		metadata.put("innerId", innerId);
		params.put("metadata", metadata);
		
		if ("send_invoice".equals(plan.getPaymentMode())) {
			params.put("collection_method", "send_invoice");
			params.put("days_until_due", 90);
		} else {
			params.put("collection_method", "charge_automatically");

			if (trialAllowed && plan.getTrialDuration() > 0) {
				params.put("trial_period_days", plan.getTrialDuration());
				Map<String, Object> trial_settings = new HashMap<>();
				Map<String, Object> end_behavior = new HashMap<>();
				trial_settings.put("end_behavior", end_behavior);
				params.put("trial_settings", trial_settings);
			}
		}

		return Subscription.create(params);
	}

	public Subscription updateSubscription(PaymentModule paymentModule, String stripeSubscriptionId, LeafPaymentPlan newPlan, LeafPaymentPlan previousPlan) throws StripeException {
		try {
			Subscription stripeSubscription = Subscription.retrieve(stripeSubscriptionId);
			if (stripeSubscription != null && !"canceled".equals(stripeSubscription.getStatus())) {
				List<SubscriptionUpdateParams.Item> items = new ArrayList<>();

		        // Remove the previous price if provided
		        if (previousPlan != null && !previousPlan.getPricing().isFree() && previousPlan.getStripePriceId() != null) {
		    		String subscriptionItemId = null;
		    		for(SubscriptionItem item : stripeSubscription.getItems().autoPagingIterable()) {
		    			if (item.getPrice().getId().equals(previousPlan.getStripePriceId())) {
		    				subscriptionItemId = item.getId();
		    			}
		    		}
		    		if (subscriptionItemId != null) {
		    			items.add(SubscriptionUpdateParams.Item.builder()
		    		            .setId(subscriptionItemId)
		    		            .setDeleted(true)
		    		            .build());
		    		}
		        }

		        // Add the new price if provided
				String newPeriod = null;
		        if (newPlan != null && !newPlan.getPricing().isFree() && newPlan.getStripePriceId() != null) {
		            items.add(
		                SubscriptionUpdateParams.Item.builder()
		                        .setPrice(newPlan.getStripePriceId())
		                        .build()
		            );
		            newPeriod = newPlan.getPricing().getPeriod();
		        }

		        if (newPeriod != null) {
		        	for(ExtraServicePrice extraServicePrice : paymentModule.getExtraServicePrices()) {
		        		if (!newPeriod.equals(extraServicePrice.period)) {
							SubscriptionUpdateParams.Item removedItem = removeSubscriptionItemFrom(stripeSubscription,
									extraServicePrice);
							if (removedItem != null) {
					            items.add(removedItem);
							}
		        			// Add new
		        			extraServicePrice.period = newPeriod;
							SubscriptionUpdateParams.Item addedItem = createSubscriptionItemFrom(extraServicePrice);
				            items.add(addedItem);
		        		}
		        	}
		        }

		        SubscriptionUpdateParams.Builder builder = SubscriptionUpdateParams.builder().addAllItem(items);
		        
		        if (newPlan != null) {
					if ("send_invoice".equals(newPlan.getPaymentMode())) {
						builder.setCollectionMethod(CollectionMethod.SEND_INVOICE);
						builder.setDaysUntilDue(90L);
					} else {
						builder.setCollectionMethod(CollectionMethod.CHARGE_AUTOMATICALLY);
					}
		        }

		        SubscriptionUpdateParams params = builder.build();
		        
		        return stripeSubscription.update(params);
			}
		} catch (StripeException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void sendUsageMetrics(String stripeSubscriptionId, String stripePriceId, long quantity)
			throws StripeException {
		Subscription subscription = Subscription.retrieve(stripeSubscriptionId);
		for (SubscriptionItem subscriptionItem : subscription.getItems().getData()) {
			if (subscriptionItem.getPrice().getId().equals(stripePriceId)) {
				UsageRecord.createOnSubscriptionItem(
						subscriptionItem.getId(), UsageRecordCreateOnSubscriptionItemParams.builder()
								.setQuantity(quantity).setTimestamp(System.currentTimeMillis() / 1000).build(),
						RequestOptions.getDefault());
				break;
			}
		}
	}

	public void updateCustomerBillingDetails(ILeafModular iModular, PaymentModule paymentModule,
			LeafAccountProfile profile) throws StripeException {
		Customer stripeCustomer = this.customerService.checkStripeCustomer(paymentModule, profile.getBillingEmail());

		// Update billing address
		Map<String, Object> updateParams = new HashMap<>();
		if (profile.getAddress() != null) {
			Map<String, Object> address = new HashMap<>();
			if (profile.getAddress().getAddress() != null) {
				address.put("line1", profile.getAddress().getAddress());
			}
			if (profile.getAddress().getPostalCode() != null) {
				address.put("postal_code", profile.getAddress().getPostalCode());
			}
			if (profile.getAddress().getCity() != null) {
				address.put("city", profile.getAddress().getCity());
			}
			if (profile.getAddress().getCountry() != null) {
				address.put("country", profile.getAddress().getCountry());
			}
			updateParams.put("address", address);
		}
		String customerName = null;
		if (profile.getFirstname() != null && !profile.getFirstname().isBlank() && profile.getLastname() != null && !profile.getLastname().isBlank()) {
			customerName = profile.getFirstname() + " " + profile.getLastname();
		}
		if (customerName == null && profile.getUsername() != null) {
			customerName = profile.getUsername();
		}
		if (profile.isCorporate() && profile.getCompanyName() != null && !profile.getCompanyName().isBlank()) {
			customerName = profile.getCompanyName();
		}
		if (customerName != null) {
			updateParams.put("name", customerName);
		}
		if (updateParams.size() > 0) {
			stripeCustomer = stripeCustomer.update(updateParams);
		}
		

		if (this.paymentConfig.isCollectTaxId() && profile.isCorporate()) {
			List<String> expandList = new ArrayList<>();
			expandList.add("tax_ids");
			Map<String, Object> params = new HashMap<>();
			params.put("expand", expandList);
			
			stripeCustomer = Customer.retrieve(paymentModule.getStripeCustomerId(), params, null);
			
			// Update tax id
			if (profile.isCorporate() && profile.getAddress() != null && profile.getAddress().getCountry() != null && profile.getTaxId() != null && !profile.getTaxId().isEmpty()) {
				TaxIdCollectionCreateParams.Type taxType = TaxIdCollectionCreateParams.Type.EU_VAT;
				switch (profile.getAddress().getCountry()) {
				case "CH":
					taxType = TaxIdCollectionCreateParams.Type.CH_VAT;
					break;
				case "FR":
				case "BE":
				case "LU":
				default:
					taxType = TaxIdCollectionCreateParams.Type.EU_VAT;
					break;
				}
				TaxIdCollectionCreateParams taxIdParams = TaxIdCollectionCreateParams.builder().setType(taxType)
						.setValue(profile.getTaxId()).build();
				TaxId existingTaxIdForType = null;
				for(TaxId tax : stripeCustomer.getTaxIds().getData()) {
					if (tax.getType().equals(taxType.getValue())) {
						existingTaxIdForType = tax;
						break;
					}
				}
				
				if (existingTaxIdForType != null) {
					if (!existingTaxIdForType.getValue().equals(profile.getTaxId())) {
						existingTaxIdForType.delete();
						stripeCustomer.getTaxIds().create(taxIdParams);
					}
				} else {
					stripeCustomer.getTaxIds().create(taxIdParams);
				}
			}
		}
		
		if (profile.getBillingEmail() != null && !profile.getBillingEmail().equalsIgnoreCase(stripeCustomer.getEmail())) {
			CustomerUpdateParams emailUpdateParams = CustomerUpdateParams.builder()
	                .setEmail(profile.getBillingEmail())
	                .build();
			stripeCustomer = stripeCustomer.update(emailUpdateParams);
		}
	}

	public void applyServiceDiffOnSubscription(PaymentModule paymentModule,
			LeafServiceSubscriptionSynchronizationAction action, String stripeSubscriptionId) {
		try {
			Subscription stripeSubscription = Subscription.retrieve(stripeSubscriptionId);
			if (stripeSubscription != null && !"canceled".equals(stripeSubscription.getStatus())) {
				List<SubscriptionUpdateParams.Item> items = new ArrayList<>();

				int subscriptionUpdateCount = 0;
		        // Remove the previous price if provided
		        for (ExtraServicePrice extraServicePrice : action.extraServicePriceToRemove) {
					SubscriptionUpdateParams.Item item = removeSubscriptionItemFrom(stripeSubscription,
							extraServicePrice);
					if (item != null) {
			            items.add(item);
			            subscriptionUpdateCount++;
					}
		            paymentModule.removeExtraServicePrice(extraServicePrice);
				}

		        // Update the previous price if provided
		        for (ExtraServicePrice extraServicePrice : action.extraServicePriceToUpdate) {					
					boolean found = false;
					String subscriptionItemId = null;
					for(SubscriptionItem item : stripeSubscription.getItems().autoPagingIterable()) {
						if (item.getPrice().getId().equals(extraServicePrice.stripePriceId)) {
							found = true;
							subscriptionItemId = item.getId();
						}
					}
					
					if (found && subscriptionItemId != null) {
						SubscriptionUpdateParams.Item.Builder builder = SubscriptionUpdateParams.Item.builder().setId(subscriptionItemId);
						builder.setQuantity((long) extraServicePrice.service.getQuantity());
                        
			            items.add(builder.build());
			            subscriptionUpdateCount++;
					}
		            paymentModule.removeExtraServicePrice(extraServicePrice);
				}

		        // Add the new price if provided
				for (ExtraServicePrice extraServicePrice : action.extraServicePriceToAdd) {
					SubscriptionUpdateParams.Item item = createSubscriptionItemFrom(extraServicePrice);
		            items.add(item);
		            subscriptionUpdateCount++;
		            paymentModule.addExtraServicePrice(extraServicePrice);
				}

		        
		        if(subscriptionUpdateCount > 0) {
			        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
			                .addAllItem(items)
			                .build();
			        stripeSubscription.update(params);
		        } else {
		        	System.out.println("There is no update - skipping update of the subscription");
		        }
			}
		} catch (StripeException e) {
			e.printStackTrace();
		}
	}

	private SubscriptionUpdateParams.Item removeSubscriptionItemFrom(Subscription stripeSubscription,
			ExtraServicePrice extraServicePrice) {
		String subscriptionItemId = null;
		for(SubscriptionItem item : stripeSubscription.getItems().autoPagingIterable()) {
			if (item.getPrice().getId().equals(extraServicePrice.stripePriceId)) {
				subscriptionItemId = item.getId();
			}
		}
		SubscriptionUpdateParams.Item item = null;
		if (subscriptionItemId != null) {
			SubscriptionUpdateParams.Item.Builder builder = SubscriptionUpdateParams.Item.builder()
		            .setId(subscriptionItemId)
		            .setDeleted(true);
			if (extraServicePrice.service.isAutomaticQuantities()) {
				builder.setClearUsage(true);
			}
			item = builder.build();
		}
		return item;
	}

	private SubscriptionUpdateParams.Item createSubscriptionItemFrom(ExtraServicePrice extraServicePrice)
			throws StripeException {
		Price newPrice = this.createPriceFrom(extraServicePrice);
		SubscriptionUpdateParams.Item.Builder builder = SubscriptionUpdateParams.Item.builder().setPrice(newPrice.getId());
		if (!extraServicePrice.service.isAutomaticQuantities()) {
			builder.setQuantity((long) extraServicePrice.service.getQuantity());
		}
		SubscriptionUpdateParams.Item item = builder.build();
		return item;
	}

	private Price createPriceFrom(ExtraServicePrice extraServicePrice) throws StripeException {
		PriceCreateParams.Recurring.Interval recurranceInterval = extraServicePrice.period.equals("year") ? PriceCreateParams.Recurring.Interval.YEAR : PriceCreateParams.Recurring.Interval.MONTH;
		
		PriceCreateParams.Builder builder = PriceCreateParams.builder().setCurrency("eur");
		if (extraServicePrice.service.getStripeProductId() != null) {
			builder.setProduct(extraServicePrice.service.getStripeProductId());
		} else {
			builder.setProductData(
	                PriceCreateParams.ProductData.builder()
                    .setName(extraServicePrice.service.getKey())
                    .build()
            );
		}
		
		long unitAmount = extraServicePrice.service.getUnitPrice();
		if (recurranceInterval == PriceCreateParams.Recurring.Interval.YEAR) {
			unitAmount *= 12;
		}
		builder.setUnitAmount(unitAmount);

		if (extraServicePrice.service.isAutomaticQuantities()) {
			builder.setRecurring(
	                PriceCreateParams.Recurring.builder()
                    .setInterval(recurranceInterval)
                    .setUsageType(PriceCreateParams.Recurring.UsageType.METERED)
                    .build()
            );
		} else {
			builder.setRecurring(
	                PriceCreateParams.Recurring.builder()
                    .setInterval(recurranceInterval)
                    .build()
            );
		}
		
		
		PriceCreateParams params = builder.build();

	    Price price = Price.create(params);
	    extraServicePrice.stripePriceId = price.getId();
	    return price;
	}
}
