package com.example.microservices.dto;



import java.util.List;

public class InviteUsersRequest {


    private List<String> emails;

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
// getters & setters
}
