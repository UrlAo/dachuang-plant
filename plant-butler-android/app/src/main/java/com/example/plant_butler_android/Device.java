package com.example.plant_butler_android;

import com.google.gson.annotations.SerializedName;

public class Device {
    private String id;
    private String name;
    @SerializedName("user_id")
    private int userId;

    public Device() {
    }

    public Device(String id, String name, int userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
