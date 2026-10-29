package org.example.domain;

import jakarta.persistence.*;


@Entity(name = "Stock")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private double quantity;



    @OneToOne(cascade = CascadeType.PERSIST)
    private RawMaterial rawMaterial;

    public Stock() {

    }


    public void setId(int id){
        this.id = id;
    }
    public void setQuantity(double amount){
        this.quantity = amount;
    }
    public int getId() {
        return id;
    }
    public double getQuantity() {
        return quantity;
    }
    public RawMaterial getRawMaterial() {
        return rawMaterial;
    }
    public void setRawMaterial(RawMaterial rawMaterial) {
        this.rawMaterial = rawMaterial;
    }


}
