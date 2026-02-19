package com.example.demo.services;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.dto.AuthenticateUserRequest;
import com.example.demo.dto.RegisterUserRequest;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.DuplicateUsernameException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_WhenValid_ShouldReturnSavedUser() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest("newuser", "new@example.com", "password");
        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = userService.registerUser(request);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getRoles().contains(Role.USER));
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_WhenUsernameExists_ShouldThrowException() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest("existing", "new@example.com", "password");
        when(userRepo.existsByUsername("existing")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateUsernameException.class, () -> userService.registerUser(request));
    }

    @Test
    void registerUser_WhenEmailExists_ShouldThrowException() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest("newuser", "existing@example.com", "password");
        when(userRepo.existsByUsername("newuser")).thenReturn(false);
        when(userRepo.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> userService.registerUser(request));
    }

    @Test
    void authenticateUser_WhenValid_ShouldReturnToken() {
        // Arrange
        AuthenticateUserRequest request = new AuthenticateUserRequest("user", "pass");
        User user = new User();
        user.setUsername("user");
        when(userDetailsService.loadUserByUsername("user")).thenReturn(user);
        when(jwtService.generateJwtToken("user")).thenReturn("mockToken");

        // Act
        String token = userService.authenticateUser(request);

        // Assert
        assertEquals("mockToken", token);
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void getAllUsers_ShouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(new User(), new User());
        Page<User> page = new PageImpl<>(users, pageable, users.size());
        when(userRepo.findAll(pageable)).thenReturn(page);

        // Act
        Page<User> result = userService.getAllUsers(pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        verify(userRepo, times(1)).findAll(pageable);
    }

    @Test
    void getUserById_WhenExists_ShouldReturnUser() {
        // Arrange
        String id = "1";
        User user = new User();
        user.setUserId(id);
        when(userRepo.findById(id)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(id);

        // Assert
        assertEquals(id, result.getUserId());
    }

    @Test
    void getUserById_WhenDoesNotExist_ShouldThrowException() {
        // Arrange
        String id = "1";
        when(userRepo.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(id));
    }
}
