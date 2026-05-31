package com.example.Slutprojekt.service;

import com.example.Slutprojekt.dto.BookingRequest;
import com.example.Slutprojekt.exception.GuestCapacityException;
import com.example.Slutprojekt.exception.ResourceNotFoundException;
import com.example.Slutprojekt.exception.RoomFullyBookedException;
import com.example.Slutprojekt.model.BookingModel;
import com.example.Slutprojekt.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    public BookingModel createBooking(BookingRequest req) {
        String roomType = req.getRoomType();

        int capacity = switch(roomType){
            case "Enkelrum" -> 1;
            case "Dubbelrum" -> 2;
            case "Svit" -> 3;
            default -> throw new ResourceNotFoundException("Okänd rumstyp: " + roomType);
        };

        if(repository.getAvailable(roomType) <= 0){
            throw new RoomFullyBookedException(roomType + " är slutbokat");
        }

        if(req.getNumberOfGuests() > capacity){
            throw new GuestCapacityException("För många gäster för rumstypen: " + roomType);
        }

        int totalPrice = switch (roomType) {
            case "Enkelrum" -> 500;
            case "Dubbelrum" -> 1000;
            case "Svit"  -> 2000;
            default -> throw new ResourceNotFoundException("Okänd rumstyp: " + roomType);
        };



        BookingModel booking = new BookingModel(
                req.getGuestName(),
                roomType,
                req.getNumberOfGuests(),
                totalPrice
        );
        repository.save(booking);
        repository.decreaseAvailable(roomType);

        return booking;
    }

    public List<BookingModel> getAllBookings() {
        return repository.findAll();
    }

    public HashMap<String, Integer> getRooms() {
        return repository.getRoomsAvailable();
    }

    public void deleteBooking(int id) {
        BookingModel booking = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bokning med id " + id + " finns inte"));

        repository.delete(booking);
        repository.increaseAvailable(booking.getRoomType());
    }
}
