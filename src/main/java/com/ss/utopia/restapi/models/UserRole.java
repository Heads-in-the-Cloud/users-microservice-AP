package com.ss.utopia.restapi.models;

import javax.persistence.*;

@Entity(name = "user_role")
public class UserRole {
    @Id
    @Column(name="id")
    private int id;

    @Column(name = "name")
    private String name;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() { return getName(); }
}
