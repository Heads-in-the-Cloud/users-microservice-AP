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

    private boolean isAdmin(User user) { return user.getRole().getName().equals("ADMIN"); }

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

        if (isAdmin(requestUser) || (requestUser.getId() == id)) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to see this user!");
    }

    @GetMapping(path={"/all", ""})
    public ResponseEntity<Iterable<User>> getAllUsers() {
        User requestUser = getUserFromAuthHeader();
        if (!isAdmin(requestUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to see other users information!");
        }

        return new ResponseEntity<>(
            userDB.findAll(),
            HttpStatus.OK
        );
    }

    @PostMapping(path = "")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        resetService.resetAutoCounter("user");
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return new ResponseEntity<>(
                userDB.save(user),
                HttpStatus.CREATED
            );
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @PutMapping(path="/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id, @RequestBody User userDetails) throws ResponseStatusException {
        User requestUser = getUserFromAuthHeader();
        if (!isAdmin(requestUser) && requestUser.getId() != id) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to update this user!");
        }

        User user = userDB
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "User not found!"
            )
        );

        if (userDetails.getUsername() != null) user.setUsername(userDetails.getUsername());
        if (userDetails.getRole() != null) user.setRole(userDetails.getRole());
        if (userDetails.getPhone() != null) user.setPhone(userDetails.getPhone());
        if (userDetails.getPassword() != null) user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        if (userDetails.getGivenName() != null) user.setGivenName(userDetails.getGivenName());
        if (userDetails.getFamilyName() != null) user.setFamilyName(userDetails.getFamilyName());
        if (userDetails.getEmail() != null) user.setEmail(userDetails.getEmail());

        try {
            User updatedUser = userDB.save(user);
            return new ResponseEntity<>(
                updatedUser,
                HttpStatus.NO_CONTENT
            );
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable int id) throws ResponseStatusException {
        User requestUser = getUserFromAuthHeader();
        if (!isAdmin(requestUser) && requestUser.getId() != id) {
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
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }
}
