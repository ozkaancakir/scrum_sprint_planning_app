package com.ozkancakir.sprintplanningapp;

public class Room {
    private String roomName;
    private String password;
    private String createdBy;

    public Room() {
    }

    public Room(String roomName, String password, String createdBy) {
        this.roomName = roomName;
        this.password = password;
        this.createdBy = createdBy;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
