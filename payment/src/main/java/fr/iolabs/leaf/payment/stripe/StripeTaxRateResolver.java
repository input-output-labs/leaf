package fr.iolabs.leaf.payment.stripe;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.iolabs.leaf.common.errors.BadRequestException;
import fr.iolabs.leaf.payment.plan.config.LeafPaymentConfig;
import fr.iolabs.leaf.payment.plan.config.StripeTaxRateMapping;

@Service
public class StripeTaxRateResolver {

	@Autowired
	private LeafPaymentConfig paymentConfig;

	public Optional<String> resolveTaxRateId(Double vatRatio) {
		if (vatRatio == null) {
			return Optional.empty();
		}
		double normalizedRatio = StripeTaxRateMapping.normalizeVatRatio(vatRatio);
		List<StripeTaxRateMapping> mappings = this.paymentConfig.getStripeTaxRates();
		if (mappings == null) {
			return Optional.empty();
		}
		for (StripeTaxRateMapping mapping : mappings) {
			if (normalizedRatio >= mapping.normalizedFrom() && normalizedRatio <= mapping.normalizedTo()) {
				return Optional.of(mapping.getStripeTaxRateId());
			}
		}
		return Optional.empty();
	}

	public String resolveTaxRateIdOrThrow(Double vatRatio) {
		return this.resolveTaxRateId(vatRatio).orElseThrow(() -> new BadRequestException(
				"No Stripe tax rate configured for VAT ratio: " + vatRatio));
	}
}
