// StepsResponse.java
package com.example.fitnesstracker.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class StepsResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("steps")
    private List<Integer> steps;
    @SerializedName("message")
    private String message;


    public boolean isSuccess() {
        return success;
    }

    public List<Integer> getSteps() {
        return steps;
    }

    public String getMessage() {
        return message;
    }
}