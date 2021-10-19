package com.ss.utopia.restapi.models;

import javax.persistence.*;

@Entity(name = "user")
public class User {

    public final static int AGENT = 0;
    public final static int USER = 1;
    public final static int GUEST = 2;
    public final static int ADMIN = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", unique = true)
    private Integer id;

    @ManyToOne
    @JoinColumn(name="role_id")
    private UserRole role;

    @Column(name="given_name")
    private String givenName;

    @Column(name="family_name")
    private String familyName;

    @Column(name="username", unique = true)
    private String username;

    @Column(name="email", unique = true)
    private String email;

    @Column(name="password")
    private String password;

    @Column(name="phone", unique = true)
    private String phone;

    public Integer getId() { return id;}
    public void setId(Integer id) { this.id = id;}

    public String getPhone() { return phone;}
    public void setPhone(String phone) { this.phone = phone;}

    public String getPassword() { return password;}
    public void setPassword(String password) { this.password = password;}

    public String getEmail() { return email;}
    public void setEmail(String email) { this.email = email;}

    public String getUsername() { return username;}
    public void setUsername(String username) { this.username = username;}

    public String getFamilyName() { return familyName;}
    public void setFamilyName(String familyName) { this.familyName = familyName;}

    public String getGivenName() { return givenName;}
    public void setGivenName(String givenName) { this.givenName = givenName;}

    public UserRole getRole() { return role;}
    public void setRole(UserRole role) { this.role = role;}

    @Override
    public String toString() {
        return getUsername() + ", " + getRole().getName();
    }
}
