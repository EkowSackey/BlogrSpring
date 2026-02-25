package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationEventListener {

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        String username = "unknown";
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        Object details = event.getAuthentication().getDetails();
        String ipAddress = "unknown";
        if (details instanceof WebAuthenticationDetails) {
            ipAddress = ((WebAuthenticationDetails) details).getRemoteAddress();
        }

        log.info("LOGIN SUCCESS: User '{}' logged in successfully from IP '{}'", username, ipAddress);
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        String username = principal.toString();

        Object details = event.getAuthentication().getDetails();
        String ipAddress = "unknown";
        if (details instanceof WebAuthenticationDetails) {
            ipAddress = ((WebAuthenticationDetails) details).getRemoteAddress();
        }

        log.warn("LOGIN FAILURE: Failed login attempt for user '{}' from IP '{}'", username, ipAddress);
    }
}
