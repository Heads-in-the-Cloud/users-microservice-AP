package com.ss.utopia.restapi.controllers;

import com.ss.utopia.restapi.dao.UserRoleRepository;
import com.ss.utopia.restapi.models.UserRole;
import com.ss.utopia.restapi.services.ResetAutoCounterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/user-roles")
public class UserRoleController {

    @Autowired
    UserRoleRepository roleDB;

    @Autowired
    ResetAutoCounterService resetService;

    @GetMapping(path="/{id}")
    public ResponseEntity<UserRole> getUser(@PathVariable int id) throws ResponseStatusException {
        return new ResponseEntity<UserRole>(roleDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "UserRole not found!")
            ),
            HttpStatus.OK
        );
    }

    @GetMapping(path="/all")
    public ResponseEntity<Iterable<UserRole>> getAllUsers() {
        return new ResponseEntity<Iterable<UserRole>>(roleDB.findAll(), HttpStatus.OK);
    }

    @PostMapping(path = "")
    public ResponseEntity<?> createUser(@RequestBody UserRole UserRole) {
        resetService.resetAutoCounter("user_role");
        try {
            return new ResponseEntity<>(
                roleDB.save(UserRole),
                HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @PutMapping(path="/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody UserRole roleDetails) throws ResponseStatusException {
        UserRole userRole = roleDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "UserRole not found!"
            )
        );

        userRole.setName(roleDetails.getName());

        try {
            UserRole updatedUser = roleDB.save(userRole);
            return new ResponseEntity<>(
                updatedUser,
                HttpStatus.NO_CONTENT
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) throws ResponseStatusException {
        UserRole userRole = roleDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "UserRole could not be found!")
            );

        try {
            roleDB.delete(userRole);
            resetService.resetAutoCounter("user_role");
            return new ResponseEntity<>(
                userRole,
                HttpStatus.NO_CONTENT
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }
}
