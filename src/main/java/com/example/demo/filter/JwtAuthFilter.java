package com.example.demo.filter;

import com.example.demo.domain.User;
import com.example.demo.services.JwtService;
import com.example.demo.services.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        // Ensure the filter runs during async dispatches to maintain SecurityContext
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwtToken = jwtService.extractTokenFromRequest(request);

        if (jwtToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if token is blacklisted
        if (tokenBlacklistService.isBlacklisted(jwtToken)) {
            log.warn("Access denied: Blacklisted token used for request to {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Validate token structure and expiration
        if (!jwtService.isTokenValid(jwtToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = jwtService.extractSubject(jwtToken);

        // Load user and set authentication (DB hit)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = (User) userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            log.info("User '{}' authenticated via JWT for endpoint '{}'", username, request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }
}
