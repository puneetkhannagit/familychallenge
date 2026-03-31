package com.example.microservices.dto;

import com.example.microservices.entity.ChallengeInvitation;

public class InvitationResponse {

    private Long id;
    private String email;
    private ChallengeInvitation.Status status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ChallengeInvitation.Status getStatus() {
        return status;
    }

    public void setStatus(ChallengeInvitation.Status status) {
        this.status = status;
    }
// getters & setters
}