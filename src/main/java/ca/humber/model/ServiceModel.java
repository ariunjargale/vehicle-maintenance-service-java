package ca.humber.model;

import java.math.BigDecimal;

public class ServiceModel {
    private int serviceId;
    private String serviceName;
    private int serviceTypeId; // changed from String to int
    private BigDecimal price;
    private boolean isActive;

    public ServiceModel() {
    }

    public ServiceModel(int serviceId, String serviceName, int serviceTypeId, BigDecimal price, boolean isActive) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceTypeId = serviceTypeId;
        this.price = price;
        this.isActive = isActive;
    }

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

    public int getServiceTypeId() {
        return serviceTypeId;
    }

    public void setServiceTypeId(int serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return serviceName + " - $" + price;
    }
}
