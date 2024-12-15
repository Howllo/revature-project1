package net.revature.project1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        // Clean the database
        userRepo.deleteAll();

        // Create test users
        createTestUser("johndoe", "John Doe", "john@example.com");
        createTestUser("janedoe", "Jane Doe", "jane@example.com");
        createTestUser("johnsmith", "John Smith", "smith@example.com");
        createTestUser("sarahconnor", "Sarah Connor", "sarah@example.com");
        createTestUser("johnjones", "John Jones", "jones@example.com");

        // Set up mock session with authenticated user
        session = new MockHttpSession();
        session.setAttribute("email", "john@example.com");
        session.setAttribute("user", "johndoe");
    }

    private void createTestUser(String username, String displayName, String email) {
        AppUser user = new AppUser(email, username, "Password123!");
        user.setDisplayName(displayName);
        userRepo.save(user);
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void searchUsers_WithValidQuery_ShouldReturnMatchingUsers() throws Exception {
        mockMvc.perform(get("/api/v1/user/search/john")
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(7))))
                .andExpect(jsonPath("$[*].username", everyItem(containsStringIgnoringCase("john"))))
                .andExpect(jsonPath("$[*].displayName").exists())
                .andExpect(jsonPath("$[*].profilePic").exists());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void searchUsers_WithEmptyQuery_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/user/search/")
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void searchUsers_WithNonExistentUsername_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/user/search/nonexistentuser")
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void searchUsers_WithPartialUsername_ShouldReturnMatchingUsers() throws Exception {
        mockMvc.perform(get("/api/v1/user/search/doe")
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].username", everyItem(containsStringIgnoringCase("doe"))));
    }

    @Test
    void searchUsers_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/user/search/john")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}