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
    public ResponseEntity<UserRole> getRole(@PathVariable int id) throws ResponseStatusException {
        return new ResponseEntity<>(roleDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "UserRole not found!")
            ),
            HttpStatus.OK
        );
    }

    @GetMapping(path={"/all", ""})
    public ResponseEntity<Iterable<UserRole>> getAllRoles() {
        return new ResponseEntity<>(roleDB.findAll(), HttpStatus.OK);
    }

    @PostMapping(path = "")
    public ResponseEntity<?> createRole(@RequestBody UserRole userRole) {
        resetService.resetAutoCounter("user_role");
        try {
            return new ResponseEntity<>(
                roleDB.save(userRole),
                HttpStatus.CREATED
            );
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRole(@PathVariable int id) throws ResponseStatusException {
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
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }
}
