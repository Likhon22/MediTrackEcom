package com.example.demo5;

import java.sql.Date;

public class customerData {
    private Integer customerId;
    private String type;
    private Integer medicineId;
    private String brand;
    private String productName;
    private Integer quantity;
    private Double price;
    private Date date;


    public customerData(Integer customerId, String type, Integer medicineId
            , String brand, String productName, Integer quantity, Double price, Date date){
        this.customerId = customerId;
        this.type = type;
        this.medicineId = medicineId;
        this.brand = brand;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
    }

    public Integer getCustomerId(){
        return customerId;
    }
    public String getType(){
        return type;
    }
    public Integer getMedicineId(){
        return medicineId;
    }
    public String getBrand(){
        return brand;
    }
    public String getProductName(){
        return productName;
    }
    public Integer getQuantity(){
        return quantity;
    }
    public Double getPrice(){
        return price;
    }
    public Date getDate(){
        return date;
    }

}
