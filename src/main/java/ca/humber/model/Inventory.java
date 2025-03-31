package ca.humber.model;


import java.math.BigDecimal;

public class Inventory {
    private Long itemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal price;
    private Integer isActive;

    public Inventory() {
    }

    public Inventory(long itemId, String itemName, int quantity, BigDecimal price) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.isActive = 1;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return itemName + " (" + quantity + ")";
    }
}
