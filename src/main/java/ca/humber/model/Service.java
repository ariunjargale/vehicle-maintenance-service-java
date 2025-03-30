package ca.humber.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "SERVICE")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICE_ID")
    private Integer serviceId;

    @Column(name = "SERVICE_NAME", nullable = false)
    private String serviceName;

    @Column(name = "SERVICE_TYPE_ID", nullable = false, length = 10)
    private String serviceTypeId;

    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    public Service() {
    }

    public Service(String serviceName, String serviceTypeId, BigDecimal price) {
        this.serviceName = serviceName;
        this.serviceTypeId = serviceTypeId;
        this.price = price;
        this.isActive = true;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return serviceName + " ($" + price + ")";
    }
}
