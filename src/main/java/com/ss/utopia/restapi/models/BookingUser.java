package com.ss.utopia.restapi.models;

import javax.persistence.*;

@Entity(name = "booking_user")
public class BookingUser {
    @Id
    @Column(name = "booking_id", nullable = false)
    private int bookingId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public BookingUser() {}
    public BookingUser(Booking booking, User user) {
        this.booking = booking;
        this.user = user;
    }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
