package com.example.spotspeak.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class MockAuthentication implements Authentication {
    private final String userId;
    private boolean authenticated = true;

    public MockAuthentication(String userId) {
        this.userId = userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public Object getCredentials() {
        return null; // No credentials
    }

    @Override
    public Object getDetails() {
        return null; // No additional details
    }

    @Override
    public Object getPrincipal() {
        return userId; // Returning the mock user ID
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return userId; // Return the mock user ID
    }
}
