package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 1, max = 12, message = "Username must be at least 1 and no more than 12 characters long")
    @Pattern(regexp = "^[a-zA-Z0-9åäöÅÄÖ!_-]+$")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 1, max = 16, message = "Password must be at least 1 and no more than 16 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!_@#-])[a-zA-ZåäöÅÄÖ0-9]+$")
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
