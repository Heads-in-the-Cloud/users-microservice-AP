package com.ss.utopia.restapi.controllers;

import com.ss.utopia.restapi.dao.UserRoleRepository;
import com.ss.utopia.restapi.models.UserRole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path="/user-roles")
public class UserRoleController {

    @Autowired
    UserRoleRepository roleDB;

    @GetMapping(path="/{id}")
    public UserRole getUser(@PathVariable int id) throws ResponseStatusException {
        return roleDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserRole not found!"));
    }

    @GetMapping(path="/all")
    public Iterable<UserRole> getAllUsers() {
        return roleDB.findAll();
    }

    @PostMapping(path = "")
    public ResponseEntity<?> createUser(@RequestBody UserRole UserRole) {
        return new ResponseEntity<>(roleDB.save(UserRole), HttpStatus.OK);
    }

    @PutMapping(path="/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody UserRole roleDetails) throws ResponseStatusException {
        UserRole userRole = roleDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserRole not found!")
        );

        userRole.setName(roleDetails.getName());

        UserRole updatedUser = roleDB.save(userRole);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) throws ResponseStatusException {
        UserRole userRole = roleDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "UserRole could not be found!"));

        roleDB.delete(userRole);
        return new ResponseEntity<>(userRole, HttpStatus.OK);
    }
}
