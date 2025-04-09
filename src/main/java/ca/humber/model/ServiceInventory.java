package ca.humber.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ServiceInventory {
    private final IntegerProperty serviceId;
    private final IntegerProperty itemId;
    private final IntegerProperty quantityRequired;
    private final StringProperty serviceName;
    private final StringProperty itemName;

    public ServiceInventory(int serviceId, int itemId, int quantityRequired) {
        this.serviceId = new SimpleIntegerProperty(serviceId);
        this.itemId = new SimpleIntegerProperty(itemId);
        this.quantityRequired = new SimpleIntegerProperty(quantityRequired);
        this.serviceName = new SimpleStringProperty("");
        this.itemName = new SimpleStringProperty("");
    }

    public ServiceInventory(int serviceId, int itemId, int quantityRequired, String serviceName, String itemName) {
        this.serviceId = new SimpleIntegerProperty(serviceId);
        this.itemId = new SimpleIntegerProperty(itemId);
        this.quantityRequired = new SimpleIntegerProperty(quantityRequired);
        this.serviceName = new SimpleStringProperty(serviceName);
        this.itemName = new SimpleStringProperty(itemName);
    }

    // Service ID
    public int getServiceId() {
        return serviceId.get();
    }

    public void setServiceId(int serviceId) {
        this.serviceId.set(serviceId);
    }

    public IntegerProperty serviceIdProperty() {
        return serviceId;
    }

    // Item ID
    public int getItemId() {
        return itemId.get();
    }

    public void setItemId(int itemId) {
        this.itemId.set(itemId);
    }

    public IntegerProperty itemIdProperty() {
        return itemId;
    }

    // Quantity Required
    public int getQuantityRequired() {
        return quantityRequired.get();
    }

    public void setQuantityRequired(int quantityRequired) {
        this.quantityRequired.set(quantityRequired);
    }

    public IntegerProperty quantityRequiredProperty() {
        return quantityRequired;
    }

    // Service Name
    public String getServiceName() {
        return serviceName.get();
    }

    public void setServiceName(String serviceName) {
        this.serviceName.set(serviceName);
    }

    public StringProperty serviceNameProperty() {
        return serviceName;
    }

    // Item Name
    public String getItemName() {
        return itemName.get();
    }

    public void setItemName(String itemName) {
        this.itemName.set(itemName);
    }

    public StringProperty itemNameProperty() {
        return itemName;
    }
}