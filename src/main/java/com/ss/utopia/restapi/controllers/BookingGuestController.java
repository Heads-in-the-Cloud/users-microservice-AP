package com.ss.utopia.restapi.controllers;

import com.ss.utopia.restapi.dao.BookingGuestRepository;
import com.ss.utopia.restapi.dao.BookingRepository;
import com.ss.utopia.restapi.models.Booking;
import com.ss.utopia.restapi.models.BookingGuest;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path="/booking")
public class BookingGuestController {

    @Autowired
    BookingGuestRepository bookingGuestDB;

    @Autowired
    BookingRepository bookingRepository;

    // Data transfer object
    class Guest {
        public String email;
        public String phone;
    }

    @GetMapping(path="/{id}/guest")
    public BookingGuest getBookingGuest(@PathVariable int id) throws ResponseStatusException {
        return bookingGuestDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingGuest not found!"));
    }

    @GetMapping(path="/guests/all")
    public Iterable<BookingGuest> getAllBookingGuests() {
        return bookingGuestDB.findAll();
    }

    @PostMapping(path = "/{id}/guest")
    public ResponseEntity<?> createBookingGuest(@PathVariable int id, @RequestBody Guest guest) {
        BookingGuest bookingGuest = new BookingGuest();
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking not found!"));

        bookingGuest.setBooking(booking);
        bookingGuest.setEmail(guest.email);
        bookingGuest.setPhone(guest.phone);
        return new ResponseEntity<>(bookingGuestDB.save(bookingGuest), HttpStatus.OK);
    }

    @PutMapping(path="/{id}/guest")
    public ResponseEntity<?> updateBookingGuest(@PathVariable int id, @RequestBody Guest guest) throws ResponseStatusException {
        BookingGuest bookingGuest = bookingGuestDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingGuest not found!")
        );

        bookingGuest.setEmail(guest.email);
        bookingGuest.setPhone(guest.phone);

        BookingGuest updatedBookingGuest = bookingGuestDB.save(bookingGuest);
        return new ResponseEntity<>(updatedBookingGuest, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/guest")
    public ResponseEntity<?> deleteBookingGuest(@PathVariable int id) throws ResponseStatusException {
        BookingGuest bookingGuest = bookingGuestDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingGuest could not be found!"));

        bookingGuestDB.delete(bookingGuest);
        return new ResponseEntity<>(bookingGuest, HttpStatus.OK);
    }
}
