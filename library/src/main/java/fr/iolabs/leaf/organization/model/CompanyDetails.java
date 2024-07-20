package fr.iolabs.leaf.organization.model;

public class CompanyDetails {
    private String companyName;
    private String address;
    private String registrationNumber;
    private String vatNumber;
    private String email;
    private String phoneNumber;

    public CompanyDetails() { }

    public CompanyDetails(String companyName, String address, String registrationNumber, String vatNumber, String email, String phoneNumber) {
        this.companyName = companyName;
        this.address = address;
        this.registrationNumber = registrationNumber;
        this.vatNumber = vatNumber;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean hasCompleteLegalInformation() {
        return companyName != null && !companyName.isEmpty()
                && address != null && !address.isEmpty()
                && registrationNumber != null && !registrationNumber.isEmpty();
    }
}