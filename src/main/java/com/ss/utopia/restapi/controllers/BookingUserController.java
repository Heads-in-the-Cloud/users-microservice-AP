package com.ss.utopia.restapi.controllers;

import com.ss.utopia.restapi.dao.BookingUserRepository;
import com.ss.utopia.restapi.dao.BookingRepository;
import com.ss.utopia.restapi.models.Booking;
import com.ss.utopia.restapi.models.BookingUser;
import com.ss.utopia.restapi.models.User;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path="/booking")
public class BookingUserController {

    @Autowired
    BookingUserRepository bookingUserDB;

    @Autowired
    BookingRepository bookingRepository;

    @GetMapping(path="/{id}/user")
    public BookingUser getBookingUser(@PathVariable int id) throws ResponseStatusException {
        return bookingUserDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingUser not found!"));
    }

    @GetMapping(path="/all/users")
    public Iterable<BookingUser> getAllBookingUsers() {
        return bookingUserDB.findAll();
    }

    @PostMapping(path = "/{id}/user")
    public ResponseEntity<?> createBookingUser(@PathVariable int id, @RequestBody User user) {
        BookingUser bookingUser = new BookingUser();
        Booking booking = bookingRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking not found!"));

        bookingUser.setBooking(booking);
        bookingUser.setUser(user);
        return new ResponseEntity<>(bookingUserDB.save(bookingUser), HttpStatus.OK);
    }

    @PutMapping(path="/{id}/user")
    public ResponseEntity<?> updateBookingUser(@PathVariable int id, @RequestBody BookingUser bookingUserDetails) throws ResponseStatusException {
        BookingUser bookingUser = bookingUserDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingUser not found!")
        );

        bookingUser.setUser(bookingUserDetails.getUser());

        BookingUser updatedBookingUser = bookingUserDB.save(bookingUser);
        return new ResponseEntity<>(updatedBookingUser, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/user")
    public ResponseEntity<?> deleteBookingUser(@PathVariable int id) throws ResponseStatusException {
        BookingUser bookingUser = bookingUserDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingUser could not be found!"));

        bookingUserDB.delete(bookingUser);
        return new ResponseEntity<>(bookingUser, HttpStatus.OK);
    }
}
