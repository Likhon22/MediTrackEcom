package com.example.demo5;

import javafx.beans.property.*;

/**
 * Data model representing an item in the shopping cart
 */
public class CartItem {
    private final IntegerProperty cartId;
    private final StringProperty username;
    private final IntegerProperty medicineId;
    private final StringProperty productName;
    private final StringProperty brand;
    private final DoubleProperty price;
    private final IntegerProperty quantity;
    private final StringProperty imagePath;
    private final DoubleProperty total;

    public CartItem(int cartId, String username, int medicineId, String productName,
                     String brand, double price, int quantity, String imagePath) {
        this.cartId = new SimpleIntegerProperty(cartId);
        this.username = new SimpleStringProperty(username);
        this.medicineId = new SimpleIntegerProperty(medicineId);
        this.productName = new SimpleStringProperty(productName);
        this.brand = new SimpleStringProperty(brand);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.imagePath = new SimpleStringProperty(imagePath);
        this.total = new SimpleDoubleProperty(price * quantity);
    }

    // Getters and setters for all properties
    public int getCartId() {
        return cartId.get();
    }

    public IntegerProperty cartIdProperty() {
        return cartId;
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public int getMedicineId() {
        return medicineId.get();
    }

    public IntegerProperty medicineIdProperty() {
        return medicineId;
    }

    public String getProductName() {
        return productName.get();
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public String getBrand() {
        return brand.get();
    }

    public StringProperty brandProperty() {
        return brand;
    }

    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
        // Update total when quantity changes
        this.total.set(this.price.get() * quantity);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public String getImagePath() {
        return imagePath.get();
    }

    public StringProperty imagePathProperty() {
        return imagePath;
    }

    public double getTotal() {
        return total.get();
    }

    public DoubleProperty totalProperty() {
        return total;
    }
}
