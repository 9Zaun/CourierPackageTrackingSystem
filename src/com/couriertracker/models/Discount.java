package com.couriertracker.models;

import java.util.UUID;

public class Discount {
    private String discountID;
    private String description;
    private double percentage;
    private boolean isActive;

    public Discount(String description, double percentage, boolean isActive) {
        this.discountID = UUID.randomUUID().toString();
        this.description = description;
        this.percentage = percentage;
        this.isActive = isActive;
    }

    public String getDiscountID() {
        return discountID;
    }

    public String getDescription() {
        return description;
    }

    public double getPercentage() {
        return percentage;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public double applyDiscount(double amount) {
        if (!isActive) {
            return amount;
        }
        return amount * (1 - percentage / 100);
    }
}
