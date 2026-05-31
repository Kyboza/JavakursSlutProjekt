package com.example.Slutprojekt.controller;

import com.example.Slutprojekt.dto.BookingRequest;
import com.example.Slutprojekt.model.BookingModel;
import com.example.Slutprojekt.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookingController {
    private final BookingService bookingService;

    BookingController(BookingService bookingService){
        this.bookingService = bookingService;
    }

    @GetMapping("/rooms")
    public HashMap<String, Integer> getRooms(){
        return bookingService.getRooms();
    }

    @GetMapping("/bookings")
    public List<BookingModel> bookingList(){
        return bookingService.getAllBookings();
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingModel> createBooking(@Valid @RequestBody BookingRequest req){
        BookingModel booking = bookingService.createBooking(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable int id){
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }
}
