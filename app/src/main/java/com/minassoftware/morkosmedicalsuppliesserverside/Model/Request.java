package com.minassoftware.morkosmedicalsuppliesserverside.Model;

import java.util.List;

/**
 * Created by Mina on 6/8/2018.
 */

public class Request {
    private String Phone;
    private String Name;
    private String Address;
    private String Total;
    private List<Order> Food;
    private String comment;
    private String Status;

    public Request() {

    }

    public Request(String phone, String name, String address, String total, List<Order> food, String comment, String status) {
        Phone = phone;
        Name = name;
        Address = address;
        Total = total;
        Food = food;
        this.comment = comment;
        Status = status;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }

    public List<Order> getFood() {
        return Food;
    }

    public void setFood(List<Order> food) {
        Food = food;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
