package com.ss.utopia.restapi.controllers;

import com.ss.utopia.restapi.dao.BookingRepository;
import com.ss.utopia.restapi.models.Booking;
import com.ss.utopia.restapi.services.ResetAutoCounterService;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path="/booking")
public class BookingController {

    @Autowired
    BookingRepository bookingDB;

    @Autowired
    ResetAutoCounterService resetService;

    @GetMapping(path="/{id}")
    public Booking getBooking(@PathVariable int id) throws ResponseStatusException {
        return bookingDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking not found!"));
    }

    @GetMapping(path="/all")
    public Iterable<Booking> getAllBookings() {
        return bookingDB.findAll();
    }

    @PostMapping(path = "")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        resetService.resetAutoCounter("booking");
        return new ResponseEntity<>(bookingDB.save(booking), HttpStatus.OK);
    }

    @PutMapping(path="/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable int id, @RequestBody Booking bookingDetails) throws ResponseStatusException {
        Booking booking = bookingDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking not found!")
        );

        booking.setIsActive(bookingDetails.getIsActive());
        booking.setConfirmationCode(bookingDetails.getConfirmationCode());

        Booking updatedBooking = bookingDB.save(booking);
        return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable int id) throws ResponseStatusException {
        Booking booking = bookingDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking could not be found!"));

        bookingDB.delete(booking);
        resetService.resetAutoCounter("booking");
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }
}
