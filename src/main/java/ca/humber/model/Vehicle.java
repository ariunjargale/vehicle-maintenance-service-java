package ca.humber.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "VEHICLE")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VEHICLE_ID")
    private Integer vehicleId;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID", nullable = false)
    private Customer customer;

    @Column(name = "MAKE", nullable = false)
    private String make;
    
    @Column(name = "MODEL", nullable = false)
    private String model;

    @Column(name = "YEAR", nullable = false)
    private Integer year;

    @Column(name = "VIN", nullable = false, unique = true, length = 17)
    private String vin;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    public Vehicle() {
    }

    public Vehicle(Customer customer, String make, String model, Integer year, String vin) {
        this.customer = customer;
        this.make = make;
        this.model = model;
        this.year = year;
        this.vin = vin;
        this.isActive = true;
    }

    // Getters and Setters
    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }
    
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (year != null) {
            sb.append(year).append(" ");
        }
        if (make != null) {
            sb.append(make).append(" ");
        }
        if (model != null) {
            sb.append(model);
        }
        if (vin != null && !vin.isEmpty()) {
            sb.append(" (").append(vin).append(")");
        }
        return sb.toString().trim();
    }
}
