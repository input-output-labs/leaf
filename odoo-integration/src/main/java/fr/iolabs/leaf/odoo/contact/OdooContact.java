package fr.iolabs.leaf.odoo.contact;

public class OdooContact {

	private Integer id;
	private String name;
	private String email;
	private String phone;
	private String mobile;
	private String companyName;
	private String street;
	private String street2;
	private String postalCode;
	private String city;
	private String country;

	public OdooContact() {}

	public OdooContact(
		Integer id,
		String name,
		String email,
		String phone,
		String mobile,
		String companyName,
		String street,
		String street2,
		String postalCode,
		String city,
		String country
	) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.mobile = mobile;
		this.companyName = companyName;
		this.street = street;
		this.street2 = street2;
		this.postalCode = postalCode;
		this.city = city;
		this.country = country;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStreet2() {
		return street2;
	}

	public void setStreet2(String street2) {
		this.street2 = street2;
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
}
