package com.ss.utopia.restapi.controllers;

import com.ss.utopia.restapi.dao.UserRepository;
import com.ss.utopia.restapi.models.User;
import com.ss.utopia.restapi.services.ResetAutoCounterService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/users")
public class UserController {

    @Autowired
    UserRepository userDB;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ResetAutoCounterService resetService;

    @Autowired
    UserDetailsService userDetailsService;

    /**
     * Gets the user from the authorization header. Otherwise, throws an exception when not found.
     * @return
     * @throws ResponseStatusException
     */
    private User getUserFromAuthHeader() throws ResponseStatusException {
        return userDB
            .findByUsername(SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString()
            ).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)
        );
    }

    @GetMapping(path="/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) throws ResponseStatusException {
        User requestUser = getUserFromAuthHeader();
        User user = userDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "User not found!")
            );

        if (requestUser.isAdmin() || (requestUser.getId() == id)) {
            return new ResponseEntity<User>(user, HttpStatus.OK);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to see this user!");

        // return new ResponseEntity<>(userDB
        //     .findById(id)
        //     .orElseThrow(() -> new ResponseStatusException(
        //         HttpStatus.BAD_REQUEST,
        //         "User not found!")
        //     ),
        //     HttpStatus.OK
        // );
    }

    @GetMapping(path={"/all", ""})
    public ResponseEntity<Iterable<User>> getAllUsers() {
        User requestUser = getUserFromAuthHeader();
        if (!requestUser.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to see other users information!");
        }

        return new ResponseEntity<>(
            userDB.findAll(),
            HttpStatus.OK
        );
    }

    @PostMapping(path = "")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        System.out.println("Creating user!");

        resetService.resetAutoCounter("user");
        try {
            System.out.println("Before Encode!");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            System.out.println("After Encode!");
            return new ResponseEntity<>(
                userDB.save(user),
                HttpStatus.CREATED
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
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody User userDetails) throws ResponseStatusException {
        User requestUser = getUserFromAuthHeader();
        if (!requestUser.isAdmin() && requestUser.getId() != id) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to update this user!");
        }

        User user = userDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "User not found!"
            )
        );

        user.setUsername(userDetails.getUsername());
        user.setRole(userDetails.getRole());
        user.setPhone(userDetails.getPhone());
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        user.setGivenName(userDetails.getGivenName());
        user.setFamilyName(userDetails.getFamilyName());
        user.setEmail(userDetails.getEmail());

        try {
            User updatedUser = userDB.save(user);
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
        User requestUser = getUserFromAuthHeader();
        if (!requestUser.isAdmin() && requestUser.getId() != id) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to delete this user!");
        }

        User user = userDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "User could not be found!")
            );

        try {
            userDB.delete(user);
            resetService.resetAutoCounter("user");
            return new ResponseEntity<>(
                user,
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
