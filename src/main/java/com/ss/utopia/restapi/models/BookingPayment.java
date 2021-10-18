package com.ss.utopia.restapi.models;

import javax.persistence.*;

@Entity(name = "booking_payment")
public class BookingPayment {
    @Id
    @Column(name = "booking_id", nullable = false)
    private int bookingId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "stripe_id")
    private String stripeId;

    @Column(name = "refunded")
    private boolean refunded;

    public BookingPayment() {}

    public BookingPayment(Booking booking, String stripeId, boolean refunded) {
        this.booking = booking;
        this.stripeId = stripeId;
        this.refunded = refunded;
    }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public boolean getRefunded() { return refunded; }
    public void setRefunded(boolean refunded) { this.refunded = refunded; }

    public String getStripeId() { return stripeId; }
    public void setStripeId(String stripeId) { this.stripeId = stripeId; }
}
