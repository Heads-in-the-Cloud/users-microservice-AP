package com.ss.utopia.restapi.controllers;

import com.ss.utopia.restapi.dao.BookingAgentRepository;
import com.ss.utopia.restapi.dao.BookingRepository;
import com.ss.utopia.restapi.models.Booking;
import com.ss.utopia.restapi.models.BookingAgent;
import com.ss.utopia.restapi.models.User;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path="/booking")
public class BookingAgentController {

    @Autowired
    BookingAgentRepository bookingAgentDB;

    @Autowired
    BookingRepository bookingRepository;

    @GetMapping(path="/{id}/agent")
    public BookingAgent getBookingAgent(@PathVariable int id) throws ResponseStatusException {
        return bookingAgentDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingAgent not found!"));
    }

    @GetMapping(path="/all/agents")
    public Iterable<BookingAgent> getAllBookingAgents() {
        return bookingAgentDB.findAll();
    }

    @PostMapping(path = "/{id}/agent")
    public ResponseEntity<?> createBookingAgent(@PathVariable int id, @RequestBody User agent) {
        BookingAgent bookingAgent = new BookingAgent();
        Booking booking = bookingRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking not found!"));

        bookingAgent.setBooking(booking);
        bookingAgent.setAgent(agent);
        return new ResponseEntity<>(bookingAgentDB.save(bookingAgent), HttpStatus.OK);
    }

    @PutMapping(path="/{id}/agent")
    public ResponseEntity<?> updateBookingAgent(@PathVariable int id, @RequestBody User agent) throws ResponseStatusException {
        BookingAgent bookingAgent = bookingAgentDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingAgent not found!")
        );

        bookingAgent.setAgent(agent);

        BookingAgent updatedBookingAgent = bookingAgentDB.save(bookingAgent);
        return new ResponseEntity<>(updatedBookingAgent, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/agent")
    public ResponseEntity<?> deleteBookingAgent(@PathVariable int id) throws ResponseStatusException {
        BookingAgent bookingAgent = bookingAgentDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingAgent could not be found!"));

        bookingAgentDB.delete(bookingAgent);
        return new ResponseEntity<>(bookingAgent, HttpStatus.OK);
    }
}
