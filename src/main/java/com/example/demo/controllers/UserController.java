package com.example.demo.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/users")
public class UserController {

    @GetMapping("/")
    public void getAllUsers(){}

    @GetMapping("/{id}")
    public void getUserById(){}

    @PostMapping("/")
    public void registerUser(){}

    @PutMapping("/{id}")
    public void updateUser(){}

    @DeleteMapping("/{id}")
    public void deleteUser(){}
}
