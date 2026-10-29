package org.example.domain;


import jakarta.persistence.*;

@Entity
@Table(name = "RawMaterials")
public class RawMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String Unit;
    private String name;


    public RawMaterial() {

    }

    public int getId(){
        return id;
    }
    public void setid(int id) {
        this.id = id;
    }
    public String getUnit() {
        return Unit;
    }
    public void setUnit(String unit) {
        Unit = unit;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
