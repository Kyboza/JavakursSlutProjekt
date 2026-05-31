package com.example.Slutprojekt.model;

public class BookingModel {
    private int id;
    private String guestName;
    private String roomType;
    private int numberOfGuests;
    private int totalPrice;

    public BookingModel(String guestName, String roomType, int numberOfGuests, int totalPrice){
        this.guestName = guestName;
        this.roomType = roomType;
        this.numberOfGuests = numberOfGuests;
        this.totalPrice = totalPrice;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName){
        this.guestName = guestName;
    }

    public String getRoomType(){
        return roomType;
    }

    public void setRoomType(String roomType){
        this.roomType = roomType;
    }

    public int getNumberOfGuests(){
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests){
        this.numberOfGuests = numberOfGuests;
    }

    public int getTotalPrice(){
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice){
        this.totalPrice = totalPrice;
    }

}
