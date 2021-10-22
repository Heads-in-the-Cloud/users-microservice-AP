package com.ss.utopia.restapi.controllers;

import com.ss.utopia.restapi.dao.UserRepository;
import com.ss.utopia.restapi.models.User;
import com.ss.utopia.restapi.services.ResetAutoCounterService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/users")
public class UserController {

    @Autowired
    UserRepository userDB;

    @Autowired
    ResetAutoCounterService resetService;

    @GetMapping(path="/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) throws ResponseStatusException {
        return new ResponseEntity<User>(userDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found!")),
            HttpStatus.OK
        );
    }

    @GetMapping(path="/all")
    public ResponseEntity<Iterable<User>> getAllUsers() {
        return new ResponseEntity<Iterable<User>>(userDB.findAll(), HttpStatus.OK);
    }

    @PostMapping(path = "")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        resetService.resetAutoCounter("user");
        try {
            return new ResponseEntity<>(userDB.save(user), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
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

        try {
            User updatedUser = userDB.save(user);
            return new ResponseEntity<>(updatedUser, HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) throws ResponseStatusException {
        User user = userDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User could not be found!"));

        try {
            userDB.delete(user);
            resetService.resetAutoCounter("user");
            return new ResponseEntity<>(user, HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
