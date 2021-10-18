package com.ss.utopia.restapi.models;

import javax.persistence.*;

@Entity(name = "booking_agent")
public class BookingAgent {
    @Id
    @Column(name = "booking_id", nullable = false)
    private int bookingId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;

    public BookingAgent() {}
    public BookingAgent(Booking booking, User agent) {
        this.booking = booking;
        this.agent = agent;
    }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public User getAgent() { return agent; }
    public void setAgent(User agent) { this.agent = agent; }
}
