package fr.iolabs.leaf.payment.plan.config;

/**
 * Maps a VAT ratio range to a Stripe tax rate id ({@code txr_...}).
 * {@code from} and {@code to} accept either a ratio ({@code 0.2} = 20%) or a percentage ({@code 20.0}).
 */
public class StripeTaxRateMapping {
	private double from;
	private double to;
	private String stripeTaxRateId;

	public double getFrom() {
		return from;
	}

	public void setFrom(double from) {
		this.from = from;
	}

	public double getTo() {
		return to;
	}

	public void setTo(double to) {
		this.to = to;
	}

	public String getStripeTaxRateId() {
		return stripeTaxRateId;
	}

	public void setStripeTaxRateId(String stripeTaxRateId) {
		this.stripeTaxRateId = stripeTaxRateId;
	}

	public double normalizedFrom() {
		return StripeTaxRateMapping.normalizeVatRatio(this.from);
	}

	public double normalizedTo() {
		return StripeTaxRateMapping.normalizeVatRatio(this.to);
	}

	public static double normalizeVatRatio(double value) {
		if (value > 1.0) {
			return value / 100.0;
		}
		return value;
	}
}
