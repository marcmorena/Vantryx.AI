package com.vantryx.api.dto;

public class SupplierRequest {
    private String name;
    private String contactName;
    private String email;
    private String phone;
    private String address;

    // 1. CONSTRUCTOR VACÍO (Vital para Jackson)
    public SupplierRequest() {
    }

    // 2. GETTERS Y SETTERS MANUALES (Para que Jackson "inyecte" los datos)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
