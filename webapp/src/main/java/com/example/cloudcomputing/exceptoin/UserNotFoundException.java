package com.example.cloudcomputing.exceptoin;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String email) {
        super("Email: " + email + "not found.");
    }
}
