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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    void registerUser_shouldRegisterUserSuccessfully() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepo.existsByUsername("testuser")).thenReturn(false);
        when(userRepo.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRoles(List.of(Role.USER));

        when(userRepo.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerUser(request);


        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.USER));

        verify(userRepo, times(1)).existsByUsername("testuser");
        verify(userRepo, times(1)).existsByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowException_whenUsernameExists() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("existinguser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepo.existsByUsername("existinguser")).thenReturn(true);

        DuplicateUsernameException exception = assertThrows(
                DuplicateUsernameException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("Username already taken", exception.getMessage());
        verify(userRepo, times(1)).existsByUsername("existinguser");
        verify(userRepo, never()).existsByEmail(anyString());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void registerUser_shouldThrowException_whenEmailExists() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepo.existsByUsername("testuser")).thenReturn(false);
        when(userRepo.existsByEmail("existing@example.com")).thenReturn(true);

        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("User with this email already exists.", exception.getMessage());
        verify(userRepo, times(1)).existsByUsername("testuser");
        verify(userRepo, times(1)).existsByEmail("existing@example.com");
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void registerUser_shouldEncodePassword() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("plainPassword");

        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerUser(request);


        assertEquals("encodedPassword", result.getPassword());
        verify(passwordEncoder, times(1)).encode("plainPassword");
    }

    @Test
    void registerUser_shouldSetDefaultUserRole() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerUser(request);


        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertEquals(Role.USER, result.getRoles().get(0));
    }

    @Test
    void registerUser_shouldSetCreatedAtDate() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerUser(request);


        assertNotNull(result.getCreatedAt());
    }

    @Test
    void authenticateUser_shouldReturnJwtToken_whenCredentialsValid() {
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPassword");

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(jwtService.generateJwtToken("testuser")).thenReturn("jwt-token-123");

        String token = userService.authenticateUser(request);


        assertNotNull(token);
        assertEquals("jwt-token-123", token);

        verify(userDetailsService, times(1)).loadUserByUsername("testuser");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateJwtToken("testuser");
    }

    @Test
    void authenticateUser_shouldCallAuthenticationManager() {
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setUsername("testuser");

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(jwtService.generateJwtToken(anyString())).thenReturn("token");

        userService.authenticateUser(request);


        verify(authenticationManager).authenticate(argThat(auth ->
                auth.getPrincipal().equals("testuser") &&
                        auth.getCredentials().equals("password123")
        ));
    }

    @Test
    void getAllUsers_shouldReturnPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);

        User user1 = new User();
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");

        List<User> users = Arrays.asList(user1, user2);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepo.findAll(pageable)).thenReturn(userPage);

        Page<User> result = userService.getAllUsers(pageable);


        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals("user1", result.getContent().get(0).getUsername());
        assertEquals("user2", result.getContent().get(1).getUsername());

        verify(userRepo, times(1)).findAll(pageable);
    }

    @Test
    void getAllUsers_shouldReturnEmptyPage_whenNoUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepo.findAll(pageable)).thenReturn(emptyPage);

        Page<User> result = userService.getAllUsers(pageable);


        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(userRepo, times(1)).findAll(pageable);
    }

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        String userId = "user123";
        User user = new User();
        user.setUserId(userId);
        user.setUsername("testuser");

        when(userRepo.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);


        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("testuser", result.getUsername());

        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void getUserById_shouldThrowException_whenUserNotFound() {
        String userId = "nonexistent";
        when(userRepo.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepo, times(1)).findById(userId);
    }

    @Test
    void registerUser_shouldSaveUserWithAllFields() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepo.existsByUsername(anyString())).thenReturn(false);
        when(userRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.registerUser(request);


        verify(userRepo).save(argThat(user ->
                user.getUsername().equals("testuser") &&
                        user.getEmail().equals("test@example.com") &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getCreatedAt() != null &&
                        user.getRoles().size() == 1 &&
                        user.getRoles().contains(Role.USER)
        ));
    }

    @Test
    void authenticateUser_shouldLoadUserBeforeAuthentication() {
        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        User user = new User();
        user.setUsername("testuser");

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(user);
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(jwtService.generateJwtToken(anyString())).thenReturn("token");

        userService.authenticateUser(request);


        var inOrder = inOrder(userDetailsService, authenticationManager, jwtService);
        inOrder.verify(userDetailsService).loadUserByUsername("testuser");
        inOrder.verify(authenticationManager).authenticate(any());
        inOrder.verify(jwtService).generateJwtToken("testuser");
    }
}
