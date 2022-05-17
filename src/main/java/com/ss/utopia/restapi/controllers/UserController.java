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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    Logger logger = LoggerFactory.getLogger(UserController.class);

    private boolean isAdmin(User user) { return user.getRole().getName().equals("ADMIN"); }

    /**
     * Gets the user from the authorization header. Otherwise, throws an exception when not found.
     * @return
     * @throws ResponseStatusException
     */
    private User getUserFromAuthHeader() throws ResponseStatusException {
        logger.info("Getting current user from Bearer Token.");

        return userDB
            .findByUsername(SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()
                .toString()
            ).orElseThrow(() -> {
                logger.warn("User not found from JWT.");
                return new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
        );
    }

    @GetMapping(path="/{id}")
    public ResponseEntity<User> getUser(@PathVariable int id) throws ResponseStatusException {
        User requestUser = getUserFromAuthHeader();
        logger.info("Getting user with id of: " + id);

        User user = userDB
            .findById(id)
            .orElseThrow(() -> {
                logger.warn("User with id: " + id + ", not found.");
                return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "User not found!");}
            );

        if (isAdmin(requestUser) || (requestUser.getId() == id)) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }

        logger.warn("Attempt was made to get user of id: " + id + " without proper authorization.");
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to see this user!");
    }

    @GetMapping(path={"/all", ""})
    public ResponseEntity<Iterable<User>> getAllUsers() {
        User requestUser = getUserFromAuthHeader();
        logger.info("Getting all users.");

        if (!isAdmin(requestUser)) {
            logger.warn("Not allowed to see other users information!");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not allowed to see other users information!");
        }

        return new ResponseEntity<>(
            userDB.findAll(),
            HttpStatus.OK
        );
    }

    @PostMapping(path = "")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        logger.info("Creating user with: " + user.toString());

        resetService.resetAutoCounter("user");
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return new ResponseEntity<>(
                userDB.save(user),
                HttpStatus.CREATED
            );
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            logger.warn(e.getMessage());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @PutMapping(path="/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id, @RequestBody User userDetails) throws ResponseStatusException {
        User requestUser = getUserFromAuthHeader();
        logger.info("Updating user with id " + id + " with: " + userDetails.toString());

        if (!isAdmin(requestUser) && requestUser.getId() != id) {
            logger.warn("Not allowed to delete this user!");
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
            logger.warn(e.getMessage());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable int id) throws ResponseStatusException {
        User requestUser = getUserFromAuthHeader();
        logger.info("Deleting user with ID: " + id);

        if (!isAdmin(requestUser) && requestUser.getId() != id) {
            logger.warn("Not allowed to delete this user!");
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
            logger.warn(e.getMessage());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }
}
