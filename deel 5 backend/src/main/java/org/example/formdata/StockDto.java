package org.example.formdata;

import jakarta.validation.constraints.Positive;

public class StockDto {
    @Positive(message = "hoeveelheid moet groter dan 0 zijn")
    private double quantity;
    private String Name;
    private int id;
    private String Unit;

    public StockDto(double quantity, String name, int id, String unit) {
        this.quantity = quantity;
        Name = name;
        this.id = id;
        Unit = unit;
    }




    public double getQuantity() {
        return quantity;
    }
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
    public String getName() {
        return Name;
    }
    public void setName(String name) {
        Name = name;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getUnit() {
        return Unit;
    }
    public void setUnit(String unit) {
        Unit = unit;
    }


}
