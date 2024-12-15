package net.revature.project1.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.revature.project1.dto.UserRequestPicDto;
import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.FileType;
import net.revature.project1.enumerator.PicUploadType;
import net.revature.project1.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import net.revature.project1.repository.PostRepo;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.resource.path=${java.io.tmpdir}/project1-test-resources/",
        "file.resource.url=http://localhost:8080/files/"
})
@AutoConfigureMockMvc
public class UserControllerProfilePicIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PostRepo postRepo;

    @Value("${app.resource.path}")
    private String resourcePath;

    private AppUser testUser;
    private MockHttpSession session;
    private String sourceImagePath = "C:\\Users\\Allen\\Pictures\\DiscordProfile.jpg";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // Clean the database
        userRepo.deleteAll();

        // Create test directories
        Path resourceDir = Path.of(resourcePath, "images");
        Files.createDirectories(resourceDir);

        // Create a test user
        testUser = new AppUser(
                "test@example.com",
                "testuser",
                "password123"
        );
        testUser.setDisplayName("Test User");
        testUser.setProfilePic("src/main/resources/static/image/Default_pfp.jpg");
        testUser = userRepo.save(testUser);

        // Set up mock session
        session = new MockHttpSession();
        session.setAttribute("email", testUser.getEmail());
        session.setAttribute("user", testUser.getUsername());

        // Create a test source file if it doesn't exist
        File sourceFile = new File(sourceImagePath);
        if (!sourceFile.exists()) {
            Path tempSourceFile = Files.createFile(tempDir.resolve("DiscordProfile.jpg"));
            // Write some test data to the file
            Files.write(tempSourceFile, "Test Image Data".getBytes());
            sourceImagePath = tempSourceFile.toString();
        }
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfilePicture_ShouldSucceed() throws Exception {
        // Create request DTO
        UserRequestPicDto requestDto = new UserRequestPicDto(
                FileType.IMAGE,
                sourceImagePath,
                "test-profile.jpg",
                PicUploadType.PROFILE_PIC
        );

        // Make the request
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/" + testUser.getId() + "/profile-pics/")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated information."));

        // Verify file was copied to correct location
        Path destinationPath = Path.of(resourcePath, "images", "test-profile.jpg");
        assertTrue(Files.exists(destinationPath), "Destination file should exist");

        // Verify file contents match
        byte[] sourceBytes = Files.readAllBytes(Path.of(sourceImagePath));
        byte[] destBytes = Files.readAllBytes(destinationPath);
        assertArrayEquals(sourceBytes, destBytes, "Source and destination files should have identical content");

        // Verify database was updated
        Optional<AppUser> updatedUser = userRepo.findById(testUser.getId());
        assertTrue(updatedUser.isPresent(), "User should exist in database");
        assertNotEquals("src/main/resources/static/image/Default_pfp.jpg",
                updatedUser.get().getProfilePic(),
                "Profile picture should be updated from default");
        assertTrue(updatedUser.get().getProfilePic().contains("test-profile.jpg"),
                "Profile picture path should contain uploaded filename");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void updateProfilePicture_WithInvalidPath_ShouldFail() throws Exception {
        UserRequestPicDto requestDto = new UserRequestPicDto(
                FileType.IMAGE,
                "nonexistent/path/image.jpg",
                "test-profile.jpg",
                PicUploadType.PROFILE_PIC
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/" + testUser.getId() + "/profile-pics/")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());

        // Verify user still has default profile picture
        Optional<AppUser> unchangedUser = userRepo.findById(testUser.getId());
        assertTrue(unchangedUser.isPresent());
        assertEquals("src/main/resources/static/image/Default_pfp.jpg",
                unchangedUser.get().getProfilePic());
    }

    @Test
    void updateProfilePicture_WithoutAuth_ShouldFail() throws Exception {
        UserRequestPicDto requestDto = new UserRequestPicDto(
                FileType.IMAGE,
                sourceImagePath,
                "test-profile.jpg",
                PicUploadType.PROFILE_PIC
        );

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/" + testUser.getId() + "/profile-pics/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }
}