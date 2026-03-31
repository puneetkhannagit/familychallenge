package com.example.microservices.controller;

import com.example.microservices.dto.ChallengeResponse;
import com.example.microservices.dto.InvitationResponse;
import com.example.microservices.dto.InviteUsersRequest;
import com.example.microservices.dto.CreateChallengeRequest;
import com.example.microservices.entity.Challenge;
import com.example.microservices.entity.ChallengeInvitation;
import com.example.microservices.entity.User;
import com.example.microservices.service.ChallengeService;
import com.example.microservices.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final UserRepository userRepository;

    public ChallengeController(ChallengeService challengeService, UserRepository userRepository) {
        this.challengeService = challengeService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeResponse createChallenge(
            @RequestBody CreateChallengeRequest request
    ) {
        User currentUser = requireCurrentUser();

        Challenge c = challengeService.createChallenge(request, currentUser);

        ChallengeResponse resp = new ChallengeResponse();
        resp.setId(c.getId());
        resp.setTitle(c.getName());

        resp.setStartsAt(c.getStartDatetime());
        resp.setEndsAt(c.getEndDatetime());
        return resp;
    }

    private User requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalArgumentException("Not authenticated");
        }

        String username = auth.getName();
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));
    }

    @PostMapping("/{challengeId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public List<InvitationResponse> invite(
            @PathVariable Long challengeId,
            @RequestBody InviteUsersRequest request
    ) {
        List<ChallengeInvitation> invitations = challengeService.invite(challengeId, request);

        return invitations.stream().map(inv -> {
            InvitationResponse resp = new InvitationResponse();
            resp.setId(inv.getId());
            resp.setEmail(inv.getEmail());
            resp.setStatus(inv.getStatus());
            return resp;
        }).toList();
    }
}
