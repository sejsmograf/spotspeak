package com.example.spotspeak.config;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Collection<GrantedAuthority> roles = extractRoles(source);
        return new JwtAuthenticationToken(source, roles);
    }

    private Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        if (jwt.getClaim("realm_access") != null) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            var mapper = new ObjectMapper();
            List<String> keycloakRoles = mapper.convertValue(realmAccess.get("roles"), List.class);
            List<GrantedAuthority> roles = new ArrayList<>();

            for (String role : keycloakRoles) {
                roles.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
            return roles;
        }

        return new ArrayList<>();
    }
}
