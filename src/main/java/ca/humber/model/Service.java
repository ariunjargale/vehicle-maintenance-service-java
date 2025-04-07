package ca.humber.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "SERVICE")
public class Service implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICE_ID")
    private int serviceId;

    @Column(name = "SERVICE_NAME", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "SERVICE_TYPE_ID")
    private String serviceTypeId;

    @Column(name = "PRICE", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    public static final String SERVICE_TYPE_REGULAR_MAINTENANCE = "1";
    public static final String SERVICE_TYPE_ENGINE_REPAIR = "2";
    public static final String SERVICE_TYPE_TRANSMISSION_REPAIR = "3";
    public static final String SERVICE_TYPE_BRAKE_SERVICE = "4";
    public static final String SERVICE_TYPE_ELECTRICAL_REPAIR = "5";
    public static final String SERVICE_TYPE_AIR_CONDITIONING = "6";
    public static final String SERVICE_TYPE_SUSPENSION_WORK = "7";
    public static final String SERVICE_TYPE_WHEEL_TIRE = "8";
    public static final String SERVICE_TYPE_DIAGNOSTIC = "9";
    public static final String SERVICE_TYPE_EXHAUST_REPAIR = "10";

    // Constructors
    public Service() {
    }

    public Service(String serviceName, String serviceTypeId, BigDecimal price) {
        this.serviceName = serviceName;
        this.serviceTypeId = serviceTypeId;
        this.price = price;
        this.isActive = true;
    }

    public Service(int serviceId, String serviceName, String serviceTypeId, BigDecimal price, Boolean isActive) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceTypeId = serviceTypeId;
        this.price = price;
        this.isActive = isActive;
    }

    // Getters and setters
    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceTypeId() {
        return serviceTypeId;
    }

    public void setServiceTypeId(String serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public String getServiceTypeName() {
        if (serviceTypeId == null) {
            return "";
        }

        switch (serviceTypeId) {
            case SERVICE_TYPE_REGULAR_MAINTENANCE:
                return "Regular Maintenance";
            case SERVICE_TYPE_ENGINE_REPAIR:
                return "Engine Repair";
            case SERVICE_TYPE_TRANSMISSION_REPAIR:
                return "Transmission Repair";
            case SERVICE_TYPE_BRAKE_SERVICE:
                return "Brake Service";
            case SERVICE_TYPE_ELECTRICAL_REPAIR:
                return "Electrical Repair";
            case SERVICE_TYPE_AIR_CONDITIONING:
                return "Air Conditioning";
            case SERVICE_TYPE_SUSPENSION_WORK:
                return "Suspension Work";
            case SERVICE_TYPE_WHEEL_TIRE:
                return "Wheel and Tire Service";
            case SERVICE_TYPE_DIAGNOSTIC:
                return "Diagnostic Service";
            case SERVICE_TYPE_EXHAUST_REPAIR:
                return "Exhaust System Repair";
            default:
                return "Unknown Service Type (" + serviceTypeId + ")";
        }
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    // Convenience methods to handle Double/BigDecimal conversion
    public Double getPriceAsDouble() {
        return price != null ? price.doubleValue() : null;
    }

    public void setPriceFromDouble(Double priceValue) {
        this.price = priceValue != null ? new BigDecimal(priceValue.toString()) : null;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return serviceName + " - $" + (price != null ? price : "0.00");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Service service = (Service) obj;
        return serviceId == service.serviceId;
    }

    @Override
    public int hashCode() {
        return 31 * serviceId;
    }
}