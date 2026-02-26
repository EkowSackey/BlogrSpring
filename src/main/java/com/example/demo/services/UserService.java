package com.example.demo.services;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.dto.AuthenticateUserRequest;
import com.example.demo.dto.RegisterUserRequest;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.DuplicateUsernameException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User registerUser(RegisterUserRequest request){
        String username = request.getUsername();
        if (userRepo.existsByUsername(username)) throw new DuplicateUsernameException("Username already taken");

        String email = request.getEmail();
        if (userRepo.existsByEmail(email)) throw new DuplicateEmailException("User with this email already exists.");

        String password = passwordEncoder.encode(request.getPassword());
        List<Role> roles = new ArrayList<>();
        roles.add( Role.READER);

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setCreatedAt(Instant.now());
        user.setRoles(roles);

        return userRepo.save(user);
    }

    @Transactional(readOnly = true)
    public String authenticateUser(AuthenticateUserRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();
        
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", user.getAuthorities());
        
        return jwtService.generateJwtToken(extraClaims, user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable){return userRepo.findAll(pageable);}

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public User getUserById(String id){
        return userRepo.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
    }

}
