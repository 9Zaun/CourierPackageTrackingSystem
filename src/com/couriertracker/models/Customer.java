package com.couriertracker.models;

import java.util.UUID;

public class Customer {
    private String customerID;
    private String password;
    private String name;
    private String address;
    private String houseNumber;
    private String streetName;
    private String city;
    private String state;
    private String pinCode;
    private String country;
    private Discount discount;

    public Customer(String password, String name, String houseNumber, String streetName, String city,
                    String state, String pinCode, String country, Discount discount) {
        this.customerID = UUID.randomUUID().toString();
        this.password = password;
        this.name = name;
        this.houseNumber = houseNumber;
        this.streetName = streetName;
        this.city = city;
        this.state = state;
        this.pinCode = pinCode;
        this.country = country;
        this.address = houseNumber + " " + streetName + ", " + city + ", " + state + ", " + pinCode + ", " + country;
        this.discount = discount;
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPinCode() {
        return pinCode;
    }

    public String getCountry() {
        return country;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }
}
