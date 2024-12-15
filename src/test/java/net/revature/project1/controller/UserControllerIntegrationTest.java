package net.revature.project1.controller;

import net.revature.project1.entity.AppUser;
import net.revature.project1.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private AppUser testUser;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        // Clean the database
        userRepo.deleteAll();

        // Create a test user
        testUser = new AppUser(
                "test@example.com",
                "testuser",
                "password123"  // Simple password for testing
        );
        testUser.setDisplayName("Test User");
        testUser.setBiography("Test biography");
        testUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        testUser = userRepo.save(testUser);

        // Set up mock session
        session = new MockHttpSession();
        session.setAttribute("email", testUser.getEmail());
        session.setAttribute("user", testUser.getUsername());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUserDto_ShouldReturnUserInfo() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/" + testUser.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.displayName", is(testUser.getDisplayName())))
                .andExpect(jsonPath("$.biography", is(testUser.getBiography())));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateUserEmail_ValidEmail_ShouldSucceed() throws Exception {
        AppUser emailUpdate = new AppUser();
        emailUpdate.setEmail("newemail@example.com");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/" + testUser.getId() + "/email")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated information."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateUserEmail_InvalidEmail_ShouldFail() throws Exception {
        AppUser emailUpdate = new AppUser();
        emailUpdate.setEmail("invalid-email");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/" + testUser.getId() + "/email")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateUsername_ShouldSucceed() throws Exception {
        AppUser usernameUpdate = new AppUser();
        usernameUpdate.setUsername("newusername");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/" + testUser.getId() + "/username")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usernameUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated information."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateDisplayName_ShouldSucceed() throws Exception {
        AppUser displayNameUpdate = new AppUser();
        displayNameUpdate.setDisplayName("New Display Name");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/" + testUser.getId() + "/display_name")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(displayNameUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated information."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateBiography_ShouldSucceed() throws Exception {
        AppUser bioUpdate = new AppUser();
        bioUpdate.setBiography("New biography text");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/" + testUser.getId() + "/biography")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bioUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated information."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void followUser_ShouldSucceed() throws Exception {
        // Create another user to follow
        AppUser userToFollow = new AppUser(
                "follow@example.com",
                "followuser",
                "password123"
        );
        userToFollow = userRepo.save(userToFollow);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/" + testUser.getId() + "/follow/" + userToFollow.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated information."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void unfollowUser_ShouldSucceed() throws Exception {
        // Create another user to unfollow
        AppUser userToUnfollow = new AppUser(
                "unfollow@example.com",
                "unfollowuser",
                "password123"
        );
        userToUnfollow = userRepo.save(userToUnfollow);

        // First follow the user
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/user/" + testUser.getId() + "/follow/" + userToUnfollow.getId())
                .session(session));

        // Then unfollow
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/user/" + testUser.getId() + "/follow/" + userToUnfollow.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated information."));
    }

    @Test
    void unauthorizedAccess_ShouldFail() throws Exception {
        // Test without @WithMockUser to verify unauthorized access fails
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/" + testUser.getId())
                        .session(session))
                .andExpect(status().isUnauthorized());
    }
}