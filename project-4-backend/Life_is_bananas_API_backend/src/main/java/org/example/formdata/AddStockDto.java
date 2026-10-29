package org.example.formdata;

import jakarta.validation.constraints.Positive;

public class AddStockDto {

    @Positive(message = "Hoeveelheid moet groter dan 0 zijn")
    private double amount;

    public AddStockDto(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
