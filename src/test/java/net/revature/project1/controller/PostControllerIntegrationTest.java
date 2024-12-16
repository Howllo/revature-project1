package net.revature.project1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.revature.project1.config.TestSecurityConfig;
import net.revature.project1.entity.AppUser;
import net.revature.project1.entity.Post;
import net.revature.project1.repository.PostRepo;
import net.revature.project1.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestSecurityConfig.class)
public class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PostRepo postRepo;

    private AppUser testUser;
    private Post mainPost;
    private Post replyPost;
    private Post deepReplyPost;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        // Clean up the database
        postRepo.deleteAll();
        userRepo.deleteAll();

        // Create test user
        testUser = new AppUser(
                "test@example.com",
                "testuser",
                "Test123!@#"
        );
        testUser.setDisplayName("Test User");
        testUser = userRepo.save(testUser);

        // Set up mock session
        session = new MockHttpSession();
        session.setAttribute("email", testUser.getEmail());
        session.setAttribute("user", testUser.getUsername());

        // Create post chain
        createPostChain();
    }

    private void createPostChain() {
        // Create main post
        mainPost = new Post();
        mainPost.setUser(testUser);
        mainPost.setComment("Main post content");
        mainPost.setPostAt(new Timestamp(System.currentTimeMillis()));
        mainPost = postRepo.save(mainPost);

        // Create first reply
        replyPost = new Post();
        replyPost.setUser(testUser);
        replyPost.setComment("First level reply");
        replyPost.setPostAt(new Timestamp(System.currentTimeMillis()));
        replyPost.setPostParent(mainPost);
        replyPost = postRepo.save(replyPost);

        // Create second reply
        deepReplyPost = new Post();
        deepReplyPost.setUser(testUser);
        deepReplyPost.setComment("Second level reply");
        deepReplyPost.setPostAt(new Timestamp(System.currentTimeMillis()));
        deepReplyPost.setPostParent(replyPost);
        deepReplyPost = postRepo.save(deepReplyPost);

        // Flush to ensure all entities are properly saved
        postRepo.flush();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getPost_ShouldReturnPostDetails() throws Exception {
        mockMvc.perform(get("/api/v1/post/" + mainPost.getId())
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.displayName", is(testUser.getDisplayName())));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createPost_WithValidData_ShouldSucceed() throws Exception {
        Post newPost = new Post();
        newPost.setUser(testUser);
        newPost.setComment("New test post content");
        newPost.setPostAt(new Timestamp(System.currentTimeMillis()));

        ResultActions result = mockMvc.perform(post("/api/v1/post/create")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPost)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.displayName", is(testUser.getDisplayName())));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void createPostChain_ThreeLevelsDeep_ShouldSucceed() throws Exception {
        // Create third level reply
        Post thirdLevelReply = new Post();
        thirdLevelReply.setUser(testUser);
        thirdLevelReply.setComment("Third level reply");
        thirdLevelReply.setPostAt(new Timestamp(System.currentTimeMillis()));
        thirdLevelReply.setPostParent(deepReplyPost);

        ResultActions result = mockMvc.perform(post("/api/v1/post/create")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(thirdLevelReply)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.displayName", is(testUser.getDisplayName())))
                .andExpect(jsonPath("$.parentPost").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updatePost_WithinTimeLimit_ShouldSucceed() throws Exception {
        Post updatePost = new Post();
        updatePost.setUser(testUser);
        updatePost.setComment("Updated content");
        updatePost.setPostAt(new Timestamp(System.currentTimeMillis()));

        ResultActions result = mockMvc.perform(put("/api/v1/post/" + mainPost.getId())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatePost)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(testUser.getUsername())))
                .andExpect(jsonPath("$.userId", is(testUser.getId().intValue())))
                .andExpect(jsonPath("$.displayName", is(testUser.getDisplayName())))
                .andExpect(jsonPath("$.postEdit", is(false)));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void deletePost_ShouldSucceed() throws Exception {
        mockMvc.perform(delete("/api/v1/post/" + mainPost.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully deleted post."));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void likePost_ShouldSucceed() throws Exception {
        mockMvc.perform(post("/api/v1/post/" + mainPost.getId() + "/like/" + testUser.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully liked post."));
    }

    @Test
    void unauthorizedAccess_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/v1/post/" + mainPost.getId()))
                .andExpect(status().isUnauthorized());

        Post newPost = new Post();
        newPost.setComment("Unauthorized post");

        mockMvc.perform(post("/api/v1/post/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost)))
                .andExpect(status().isUnauthorized());
    }
}