package net.revature.project1.controller;

import net.revature.project1.dto.UserSearchDto;
import net.revature.project1.entity.AppUser;
import net.revature.project1.repository.UserRepo;
import net.revature.project1.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerSearchIntegrationTest {

    @Mock
    private SearchService searchService;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private SearchController searchController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(searchController).build();
    }

    @Test
    void searchUser_Success() throws Exception {
        List<UserSearchDto> mockResults = Arrays.asList(
                new UserSearchDto("john.doe", "John Doe", "/default.jpg"),
                new UserSearchDto("johnny", "Johnny Test", "/default.jpg")
        );

        when(searchService.getSearchUser(anyString())).thenReturn(mockResults);

        mockMvc.perform(get("/api/v1/search/user/john"))
                .andExpect(status().isOk());
    }
}