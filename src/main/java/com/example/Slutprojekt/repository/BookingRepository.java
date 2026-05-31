package com.example.Slutprojekt.repository;

import com.example.Slutprojekt.model.BookingModel;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepository {
    private final List<BookingModel> bookingList = new ArrayList<>();
    private int nextId = 1;
    private final HashMap<String, Integer> roomsAvailable = new HashMap<>();

    BookingRepository(){
        roomsAvailable.put("Enkelrum", 10);
        roomsAvailable.put("Dubbelrum", 7);
        roomsAvailable.put("Svit", 3);
    }

    public List<BookingModel> findAll(){
        return bookingList;
    }

    public void save(BookingModel booking){
        booking.setId(nextId++);
        bookingList.add(booking);
    }

    public int getAvailable(String roomType){
        return roomsAvailable.get(roomType);
    }

    public HashMap<String, Integer> getRoomsAvailable() {
        return roomsAvailable;
    }

    public void decreaseAvailable(String roomType) {
        roomsAvailable.put(roomType, roomsAvailable.get(roomType) - 1);
    }

    public void increaseAvailable(String roomType) {
        roomsAvailable.put(roomType, roomsAvailable.get(roomType) + 1);
    }

    public Optional<BookingModel> findById(int id) {
        return bookingList.stream()
                .filter(booking -> booking.getId() == id)
                .findFirst();
    }

    public void delete(BookingModel booking) {
        bookingList.remove(booking);
    }

}
