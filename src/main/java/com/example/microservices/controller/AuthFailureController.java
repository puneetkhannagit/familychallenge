// java
package com.example.microservices.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthFailureController {

    @GetMapping(value = "/auth/failure", produces = MediaType.TEXT_HTML_VALUE)
    public String failure() {
        return "<html><body><h3>OAuth login failed</h3><p>Please try again.</p></body></html>";
    }
}
