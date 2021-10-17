package com.ss.utopia.restapi.controllers;

import com.ss.utopia.restapi.dao.UserRepository;
import com.ss.utopia.restapi.models.User;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path="/users")
public class UserController {

    @Autowired
    UserRepository userDB;

    @GetMapping(path="/{id}")
    public User getUser(@PathVariable int id) throws ResponseStatusException {
        return userDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found!"));
    }

    @GetMapping(path="/all")
    public Iterable<User> getAllUsers() {
        return userDB.findAll();
    }

    @PostMapping(path = "")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        return new ResponseEntity<>(userDB.save(user), HttpStatus.OK);
    }

    @PutMapping(path="/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody User userDetails) throws ResponseStatusException {
        User user = userDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found!")
        );

        user.setUsername(userDetails.getUsername());
        user.setRole(userDetails.getRole());
        user.setPhone(userDetails.getPhone());
        user.setPassword(userDetails.getPassword());
        user.setGivenName(userDetails.getGivenName());
        user.setFamilyName(userDetails.getFamilyName());
        user.setEmail(userDetails.getEmail());

        User updatedUser = userDB.save(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) throws ResponseStatusException {
        User user = userDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User could not be found!"));

        userDB.delete(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
