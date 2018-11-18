package com.car.carsquad.carapp;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {

    //User fields
    public String userID;
    public String firstName;
    public String lastName;
    public String phoneNo;
    public String isDriver;
    public double driverRating;

    public String currentMode;

    //link to database
    //private DatabaseReference mDatabase;
    //mDatabase = FirebaseDatabase.getInstance().getReference();


    public User(){
        //default constructor
    }

    public User(String userID, String firstName, String lastName, String phoneNo,
                 String isDriver, double driverRating){
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNo = phoneNo;
        this.isDriver = isDriver;
        this.driverRating = driverRating;
        this.currentMode = "rider";
    }

    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPhoneNo() {
        return phoneNo;
    }
    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
    public String isDriver() {
        return isDriver;
    }
    public void setDriver(String driver) {
        isDriver = driver;
    }
    public double getDriverRating() {
        return driverRating;
    }
    public void setDriverRating(double driverRating) {
        this.driverRating = driverRating;
    }

    public String getCurrentMode() {
        return currentMode;
    }
    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }
}
