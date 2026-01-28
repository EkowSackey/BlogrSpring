package com.example.demo.services;

import com.example.demo.domain.User;
import com.example.demo.dto.UserRegistrationRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @CacheEvict(value = "users", allEntries = true)
    public User registerUser(UserRegistrationRequest request){
        if (userRepo.existsByUsername(request.getUsername())){
            throw new BadRequestException("Username already taken");
        }

//        User user = new User(request.getUsername(), request.getEmail(), request.getPassword());
//        return userRepo.save(user);
        return null;
    }

    @Cacheable(value = "users", key = "#userid")
    public User getUserById(String id){
        return userRepo.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));
    }

}
