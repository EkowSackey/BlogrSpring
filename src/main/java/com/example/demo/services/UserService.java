package com.example.demo.services;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.dto.AuthenticateUserRequest;
import com.example.demo.dto.RegisterUserRequest;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.DuplicateUsernameException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j

public class UserService {

    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public UserService(UserRepository userRepo, JwtService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }


    @CacheEvict(value = "users", allEntries = true)
    public User registerUser(RegisterUserRequest request){
        String username = request.getUsername();
        if (userRepo.existsByUsername(username)) throw new DuplicateUsernameException("Username already taken");

        String email = request.getEmail();
        if (userRepo.existsByEmail(email)) throw new DuplicateEmailException("User with this email already exists.");

        String password = passwordEncoder.encode(request.getPassword());
        List<Role> roles = new ArrayList<>();
        roles.add( Role.USER);

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setCreatedAt(new Date());
        user.setRoles(roles);

        return userRepo.save(user);
    }

    public String authenticateUser(AuthenticateUserRequest request){
        User user = (User) userDetailsService.loadUserByUsername(request.getUsername());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));
        return jwtService.generateJwtToken(request.getUsername());
    }

    public Page<User> getAllUsers(Pageable pageable){return userRepo.findAll(pageable);}

    @Cacheable(value = "users", key = "#userid")
    public User getUserById(String id){
        return userRepo.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
    }

}
