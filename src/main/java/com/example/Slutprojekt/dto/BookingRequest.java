package com.example.Slutprojekt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Det klienten skickar in vid POST /api/bookings.
 * id och totalPrice sätts av servern och finns därför inte här.
 */
public class BookingRequest {

    @NotBlank(message = "Namn får inte vara tomt")
    @Pattern(regexp = "^[a-zA-ZåäöÅÄÖ -]+$", message = "Namnet innehåller otillåtna tecken")
    private String guestName;

    @NotBlank(message = "Rumstyp får inte vara tom")
    @Pattern(regexp = "Enkelrum|Dubbelrum|Svit", message = "Rumstyp måste vara Enkelrum, Dubbelrum eller Svit")
    private String roomType;

    @Min(value = 1, message = "Antal gäster måste vara minst 1")
    @Max(value = 3, message = "Antal gäster får vara högst 3")
    private int numberOfGuests;

    public BookingRequest() {
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }
}
