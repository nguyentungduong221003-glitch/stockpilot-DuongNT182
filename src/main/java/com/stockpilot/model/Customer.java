package com.stockpilot.model;

import com.stockpilot.exception.InvalidInputException;
import java.util.Objects;
import java.util.regex.Pattern;

public class Customer {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9\\s-]{9,20}$");

    private String id;
    private String name;
    private String email;
    private String phone;

    public Customer() {}

    public Customer(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        setEmail(email);
        setPhone(phone);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidInputException("Invalid email format: " + email);
        }
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidInputException("Invalid phone format: " + phone);
        }
        this.phone = phone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id) || Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return String.format("Customer[ID='%s', Name='%s', Email='%s', Phone='%s']",
                id, name, email, phone);
    }
}
