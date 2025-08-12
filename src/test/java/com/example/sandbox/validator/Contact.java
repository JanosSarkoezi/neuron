package com.example.sandbox.validator;

import java.util.Objects;

public class Contact {
    private String email;
    private String phone;
    private String type; // z.B. "PRIVATE", "BUSINESS"

    public Contact() {
    }

    public Contact(String email) {
        this.email = email;
    }

    public Contact(String email, String phone, String type) {
        this.email = email;
        this.phone = phone;
        this.type = type;
    }

    // Getter und Setter
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(email, contact.email) &&
                Objects.equals(phone, contact.phone) &&
                Objects.equals(type, contact.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, phone, type);
    }

    @Override
    public String toString() {
        return "Contact{" +
                "email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}