package com.example.demo.config;

import com.example.demo.domain.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.error("OAuth2 login successful for email {}, but user record not found in database.", email);
            sendErrorResponse(response, "User record could not be synchronized. Please try again.");
            return;
        }

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", user.getAuthorities());

        String token = jwtService.generateJwtToken(extraClaims, user);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, String> tokenResponse = new HashMap<>();
        tokenResponse.put("token", token);
        
        response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("code", "OAUTH2_SYNC_ERROR");
        errorBody.put("message", message);
        errorBody.put("timestamp", java.time.Instant.now().toString());

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
