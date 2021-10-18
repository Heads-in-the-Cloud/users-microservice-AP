package com.ss.utopia.restapi.models;

import java.io.Serializable;

import javax.persistence.*;

import jdk.jfr.Unsigned;

@Entity(name = "booking")
public class Booking implements Serializable {
    @Id()
    @Unsigned
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private Integer id;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "confirmation_code")
    private String confirmationCode;

    public Booking() {};

    public Booking(int id, boolean isActive, String code) {
        this.id = id;
        this.isActive = isActive;
        this.confirmationCode = code;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }

    public String getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(String confirmationCode) { this.confirmationCode = confirmationCode; }
}
