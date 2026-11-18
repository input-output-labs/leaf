package fr.iolabs.leaf.odoo.product;

/** Quantities read from Odoo for one product. Null means the value was not available. */
public class OdooProductStock {

	private Integer productId;
	/** Physical on-hand quantity (`qty_available`). */
	private Double qtyAvailable;
	/** Confirmed incoming quantity not yet received (`incoming_qty`). */
	private Double incomingQty;
	/**
	 * Minimum from the single active reordering rule (`stock.warehouse.orderpoint.product_min_qty`).
	 * Null when the product has no rule, or more than one.
	 */
	private Double minQty;

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Double getQtyAvailable() {
		return qtyAvailable;
	}

	public void setQtyAvailable(Double qtyAvailable) {
		this.qtyAvailable = qtyAvailable;
	}

	public Double getIncomingQty() {
		return incomingQty;
	}

	public void setIncomingQty(Double incomingQty) {
		this.incomingQty = incomingQty;
	}

	public Double getMinQty() {
		return minQty;
	}

	public void setMinQty(Double minQty) {
		this.minQty = minQty;
	}
}
