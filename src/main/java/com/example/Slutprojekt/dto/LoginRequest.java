package com.example.Slutprojekt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "Användarnamn får inte vara tomt")
    @Size(min = 1, max = 12, message = "Användarnamn måste vara mellan 1 och 12 tecken långt")
    @Pattern(regexp = "^[a-zA-Z0-9åäöÅÄÖ!_-]+$")
    private String username;

    @NotBlank(message = "Lösenord får inte vara tomt")
    @Size(min = 1, max = 16, message = "Lösenord måste vara mellan 1 och 16 tecken långt")
    private String password;

    LoginRequest(){}

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username){
        String trimmed = username.trim();
        String firstLetter = trimmed.substring(0, 1).toUpperCase();
        String rest = trimmed.substring(1).toLowerCase();
        this.username = firstLetter + rest;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String password){
        this.password = password.trim();
    }
}
