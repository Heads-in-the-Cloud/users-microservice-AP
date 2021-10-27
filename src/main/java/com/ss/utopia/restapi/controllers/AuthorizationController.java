package com.ss.utopia.restapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorizationController {

    @PostMapping(path = "/login")
    public ResponseEntity<?> login()
    {
        return null;
    }

    @PostMapping(path = "/logout")
    public ResponseEntity<?> logout()
    {
        return null;
    }
}
