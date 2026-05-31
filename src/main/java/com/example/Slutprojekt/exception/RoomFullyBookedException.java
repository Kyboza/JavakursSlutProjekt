package com.example.Slutprojekt.exception;

public class RoomFullyBookedException extends RuntimeException {
    public RoomFullyBookedException(String message) {
        super(message);
    }
}
