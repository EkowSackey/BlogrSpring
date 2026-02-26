package com.example.demo.services;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            // Create new user if they don't exist
            User user = new User();
            user.setEmail(email);
            
            // Generate a unique username
            String baseUsername = generateBaseUsername(email, oAuth2User.getAttribute("name"));
            user.setUsername(ensureUniqueUsername(baseUsername));
            
            user.setRoles(Collections.singletonList(Role.READER));
            user.setCreatedAt(Instant.now());
            userRepository.save(user);
        }
        // If user exists, we just return the oAuth2User

        return oAuth2User;
    }

    private String generateBaseUsername(String email, String name) {
        if (email != null) {
            return email.split("@")[0];
        }
        if (name != null) {
            return name.toLowerCase().replaceAll("\\s+", "");
        }
        return "user";
    }

    private String ensureUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            // If collision, append a short random string or a counter
            String suffix = UUID.randomUUID().toString().substring(0, 4);
            username = baseUsername + "_" + suffix;
            
            if (counter++ > 10) {
                username = baseUsername + "_" + UUID.randomUUID().toString();
                break;
            }
        }
        return username;
    }
}
