package com.example.dataset.controller;

import io.swagger.v3.oas.annotations.Hidden;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Sends visitors of the base URL to the API documentation. */
@RestController
@Hidden
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Void> home() {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/swagger-ui.html")).build();
    }
}