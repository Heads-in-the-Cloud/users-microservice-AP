package com.ss.utopia.restapi.controllers;

import com.ss.utopia.restapi.dao.BookingPaymentRepository;
import com.ss.utopia.restapi.dao.BookingRepository;
import com.ss.utopia.restapi.models.Booking;
import com.ss.utopia.restapi.models.BookingPayment;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path="/booking")
public class BookingPaymentController {

    @Autowired
    BookingPaymentRepository bookingPaymentDB;

    @Autowired
    BookingRepository bookingRepository;

    // Data transfer object
    class Payment {
        public Boolean refunded;
        public String stripeId;
    }

    @GetMapping(path="/{id}/payment")
    public BookingPayment getBookingPayment(@PathVariable int id) throws ResponseStatusException {
        return bookingPaymentDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingPayment not found!"));
    }

    @GetMapping(path="/all/payments")
    public Iterable<BookingPayment> getAllBookingPayments() {
        return bookingPaymentDB.findAll();
    }

    @PostMapping(path = "/{id}/payment")
    public ResponseEntity<?> createBookingPayment(@PathVariable int id, @RequestBody Payment payment) {
        BookingPayment bookingPayment = new BookingPayment();
        Booking booking = bookingRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking not found!"));

        bookingPayment.setBooking(booking);
        bookingPayment.setRefunded(payment.refunded);
        bookingPayment.setStripeId(payment.stripeId);
        return new ResponseEntity<>(bookingPaymentDB.save(bookingPayment), HttpStatus.OK);
    }

    @PutMapping(path="/{id}/payment")
    public ResponseEntity<?> updateBookingPayment(@PathVariable int id, @RequestBody Payment payment) throws ResponseStatusException {
        BookingPayment bookingPayment = bookingPaymentDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingPayment not found!")
        );

        bookingPayment.setRefunded(payment.refunded);
        bookingPayment.setStripeId(payment.stripeId);

        BookingPayment updatedBookingPayment = bookingPaymentDB.save(bookingPayment);
        return new ResponseEntity<>(updatedBookingPayment, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/payment")
    public ResponseEntity<?> deleteBookingPayment(@PathVariable int id) throws ResponseStatusException {
        BookingPayment bookingPayment = bookingPaymentDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "BookingPayment could not be found!"));

        bookingPaymentDB.delete(bookingPayment);
        return new ResponseEntity<>(bookingPayment, HttpStatus.OK);
    }
}
