package com.cpt202.projectselection.service;

import java.time.LocalDateTime;

public class ActivationNotice {

    private final String email;
    private final String token;
    private final LocalDateTime expiresAt;

    public ActivationNotice(String email, String token, LocalDateTime expiresAt) {
        this.email = email;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
