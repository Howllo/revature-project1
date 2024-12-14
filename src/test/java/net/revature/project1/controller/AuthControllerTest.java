package net.revature.project1.controller;

import jakarta.servlet.http.HttpSession;
import net.revature.project1.dto.AuthResponseDto;
import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.AuthEnum;
import net.revature.project1.result.AuthResult;
import net.revature.project1.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthController authController;

    private AppUser testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new AppUser();
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("Password123!");
    }

    // Registration Tests
    @Test
    void register_Success() {
        // Arrange
        AppUser returnUser = new AppUser();
        returnUser.setEmail(testUser.getEmail());
        returnUser.setUsername(testUser.getUsername());

        when(authService.registration(any(AppUser.class)))
                .thenReturn(new AuthResult(AuthEnum.CREATED, returnUser));

        // Act
        ResponseEntity<?> response = authController.register(testUser, session);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponseDto);
        verify(session).setAttribute("email", testUser.getEmail());
        verify(session).setAttribute("user", testUser.getUsername());
    }

    @Test
    void register_EmailTaken() {
        // Arrange
        when(authService.registration(any(AppUser.class)))
                .thenReturn(new AuthResult(AuthEnum.EMAIL_TAKEN, testUser));

        // Act
        ResponseEntity<?> response = authController.register(testUser, session);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already registered", response.getBody());
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    void register_UsernameTaken() {
        // Arrange
        when(authService.registration(any(AppUser.class)))
                .thenReturn(new AuthResult(AuthEnum.USERNAME_TAKEN, testUser));

        // Act
        ResponseEntity<?> response = authController.register(testUser, session);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username already in used", response.getBody());
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    void register_InvalidCredentials() {
        // Arrange
        when(authService.registration(any(AppUser.class)))
                .thenReturn(new AuthResult(AuthEnum.INVALID_CREDENTIALS, testUser));

        // Act
        ResponseEntity<?> response = authController.register(testUser, session);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad email or password", response.getBody());
        verify(session, never()).setAttribute(anyString(), any());
    }

    // Login Tests
    @Test
    void login_Success() {
        // Arrange
        AppUser returnUser = new AppUser();
        returnUser.setEmail(testUser.getEmail());
        returnUser.setUsername(testUser.getUsername());
        returnUser.setDisplayName("Test User");

        when(authService.login(any(AppUser.class)))
                .thenReturn(new AuthResult(AuthEnum.SUCCESS, returnUser));

        // Act
        ResponseEntity<?> response = authController.login(testUser, session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponseDto);
        verify(session).setAttribute("email", testUser.getEmail());
        verify(session).setAttribute("user", returnUser.getUsername());
    }

    @Test
    void login_InvalidCredentials() {
        // Arrange
        when(authService.login(any(AppUser.class)))
                .thenReturn(new AuthResult(AuthEnum.INVALID_CREDENTIALS, testUser));

        // Act
        ResponseEntity<?> response = authController.login(testUser, session);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password. Please try again.", response.getBody());
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    void login_UnknownError() {
        // Arrange
        when(authService.login(any(AppUser.class)))
                .thenReturn(new AuthResult(AuthEnum.UNKNOWN, testUser));

        // Act
        ResponseEntity<?> response = authController.login(testUser, session);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Internal Server Error"));
        verify(session, never()).setAttribute(anyString(), any());
    }

    // Logout Test
    @Test
    void logout_Success() {
        // Act
        ResponseEntity<String> response = authController.logout(session);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully logged out", response.getBody());
        verify(session).invalidate();
    }
}
