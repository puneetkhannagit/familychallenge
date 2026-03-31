package com.example.microservices.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/me")
public class MeController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String username = auth.getName();
        String email = username;
        String name = null;

        Object principal = auth.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            if (oidcUser.getEmail() != null) {
                email = oidcUser.getEmail();
            }
            name = oidcUser.getFullName(); // may be null
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("authenticated", true);
        body.put("username", username);
        body.put("roles", roles);
        if (email != null) body.put("email", email);
        if (name != null) body.put("name", name);

        return ResponseEntity.ok(body);
    }
}
