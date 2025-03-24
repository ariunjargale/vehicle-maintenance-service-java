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

    @Column(name = "MODEL", nullable = false)
    private String model;

    @Column(name = "YEAR", nullable = false)
    private Integer year;

    @Column(name = "VIN", nullable = false, unique = true, length = 17)
    private String vin;

    @Column(name = "LICENSE_PLATE", nullable = false, unique = true, length = 50)
    private String licensePlate;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    // 無參數建構函式
    public Vehicle() {
    }

    // 有參數建構函式
    public Vehicle(Customer customer, String model, Integer year, String vin, String licensePlate) {
        this.customer = customer;
        this.model = model;
        this.year = year;
        this.vin = vin;
        this.licensePlate = licensePlate;
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

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return year + " " + model + " (" + licensePlate + ")";
    }
}
