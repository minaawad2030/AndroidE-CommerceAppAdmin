package com.minassoftware.morkosmedicalsuppliesserverside.Model;

/**
 * Created by Mina on 6/4/2018.
 */

public class User {

    private String name;
    private String password;
    private String phone;
    private String isStaff;
    public User() {
    }

    public User(String Pname, String Ppassword) {


        name = Pname;
        password = Ppassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String setname(String Pname) {
        name = Pname;
        return name;
    }

    public String getName() {
        return name;
    }

    public String getIsStaff() {
        return isStaff;
    }

    public void setIsStaff(String isStaff) {
        this.isStaff = isStaff;
    }

    public String getpassword() {
        return password;
    }
}
