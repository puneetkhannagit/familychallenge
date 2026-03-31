// java
package com.example.microservices.service;


import com.example.microservices.dto.SignupRequest;
import com.example.microservices.entity.User;
import com.example.microservices.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public SignupService(UserRepository users, PasswordEncoder encoder) {
        this.userRepository = users;
        this.encoder = encoder;
    }

    public void signup(SignupRequest req) {
        userRepository.findByUsername(req.getUsername()).ifPresent(u -> {
            throw new IllegalStateException("username already exists");
        });
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        // set defaults as per your schema, e.g., enabled, roles/authorities
        // user.setEnabled(true);
        userRepository.save(user);
    }
}



