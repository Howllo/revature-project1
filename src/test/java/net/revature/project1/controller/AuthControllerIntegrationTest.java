package net.revature.project1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.revature.project1.config.TestSecurityConfig;
import net.revature.project1.entity.AppUser;
import net.revature.project1.repository.AuthRepo;
import net.revature.project1.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestSecurityConfig.class)
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthRepo authRepo;

    private AppUser validUser;
    private AppUser existingUser;

    @BeforeEach
    void setUp() {
        // Clean up the database
        authRepo.deleteAll();
        userRepo.deleteAll();

        // Create a valid test user
        validUser = new AppUser(
                "test@example.com",
                "testuser",
                "Test123!@#"
        );

        // Create and save an existing user
        existingUser = new AppUser(
                "existing@example.com",
                "existinguser",
                "Existing123!@#"
        );
        authRepo.save(existingUser);
    }

    @Test
    void registerUser_WithValidData_ShouldSucceed() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser)));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("Successfully created a account.")))
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldFail() throws Exception {
        // Arrange
        AppUser userWithExistingEmail = new AppUser(
                "existing@example.com",
                "newusername",
                "Test123!@#"
        );

        // Act
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userWithExistingEmail)));

        // Assert
        result.andExpect(status().isConflict())
                .andExpect(content().string("Email already registered"));
    }

    @Test
    void registerUser_WithInvalidEmail_ShouldFail() throws Exception {
        // Arrange
        AppUser userWithInvalidEmail = new AppUser(
                "invalid-email",
                "testuser",
                "Test123!@#"
        );

        // Act
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userWithInvalidEmail)));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(content().string("Bad email or password"));
    }

    @Test
    void registerUser_WithInvalidPassword_ShouldFail() throws Exception {
        // Arrange
        AppUser userWithInvalidPassword = new AppUser(
                "test@example.com",
                "testuser",
                "weak"
        );

        // Act
        ResultActions result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userWithInvalidPassword)));

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(content().string("Bad email or password"));
    }

    @Test
    void login_WithValidCredentials_ShouldSucceed() throws Exception {
        // Arrange - First register a user
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser)));

        // Act - Then try to login
        ResultActions result = mockMvc.perform(get("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser)));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Login successful!")))
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    void login_WithInvalidCredentials_ShouldFail() throws Exception {
        // Arrange
        AppUser invalidUser = new AppUser(
                "nonexistent@example.com",
                "testuser",
                "Test123!@#"
        );

        // Act
        ResultActions result = mockMvc.perform(get("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)));

        // Assert
        result.andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password. Please try again."));
    }

    @Test
    void logout_ShouldSucceed() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/api/v1/auth/logout"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(content().string("Successfully logged out"));
    }
}