package fr.iolabs.leaf.authentication.model.profile;

import java.util.HashMap;
import java.util.Map;

public class LeafAddress {
	private String address;
	private String postalCode;
	private String city;
	private String country;

	public static LeafAddress from(LeafAddress from) {
		LeafAddress newAddress = new LeafAddress();
		newAddress.updateWith(from);
		return newAddress;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> object = new HashMap<String, Object>();
		object.put("address", this.address);
		object.put("postalCode", this.postalCode);
		object.put("city", this.city);
		object.put("country", this.country);
		return object;
	}

	public void updateWith(LeafAddress updates) {
		if (updates.address != null) {
			this.address = updates.address;
		}
		if (updates.postalCode != null) {
			this.postalCode = updates.postalCode;
		}
		if (updates.city != null) {
			this.city = updates.city;
		}
		if (updates.country != null) {
			this.country = updates.country;
		}
	}
}
