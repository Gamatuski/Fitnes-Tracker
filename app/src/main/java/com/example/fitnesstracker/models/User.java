package com.example.fitnesstracker.models;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("_id") // Указываем соответствие JSON полю "_id"
    private String id; // Поле для хранения ID пользователя

    private String email;
    private String password; // Внимание: пароль не должен передаваться обратно с сервера в production
    private int height;
    private int weight;


    // Конструктор по умолчанию (необходим для Gson)
    public User() {
    }


    // Конструктор
    public User(String email, String password, int height, int weight) {
        this.email = email;
        this.password = password;
        this.height = height;
        this.weight = weight;
    }

    // Геттеры и сеттеры

    public String getId() { // Геттер для ID
        return id;
    }

    public void setId(String id) { // Сеттер для ID
        this.id = id;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}