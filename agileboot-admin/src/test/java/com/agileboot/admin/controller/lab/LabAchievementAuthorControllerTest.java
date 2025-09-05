package com.agileboot.admin.controller.lab;

import com.agileboot.domain.lab.achievement.db.LabAchievementAuthorEntity;
import com.agileboot.domain.lab.achievement.db.LabAchievementAuthorService;
import com.agileboot.domain.lab.achievement.db.LabAchievementService;
import com.agileboot.domain.lab.user.LabUserPermissionChecker;
import com.agileboot.domain.lab.user.db.LabUserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabAchievementAuthorController.class)
class LabAchievementAuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LabAchievementService achievementService;

    @MockBean
    private LabAchievementAuthorService authorService;

    @MockBean
    private LabUserPermissionChecker labUserPermissionChecker;

    private LabUserEntity mockUser;
    private LabAchievementAuthorEntity mockAuthor;

    @BeforeEach
    void setUp() {
        mockUser = new LabUserEntity();
        mockUser.setId(100L);
        mockUser.setRealName("Test User");
        mockUser.setIdentity(1); // 管理员

        mockAuthor = new LabAchievementAuthorEntity();
        mockAuthor.setId(1L);
        mockAuthor.setAchievementId(1L);
        mockAuthor.setUserId(5L);
        mockAuthor.setName("张三");
        mockAuthor.setAuthorOrder(1);
        mockAuthor.setIsCorresponding(false);
        mockAuthor.setRole("第一作者");
        mockAuthor.setVisible(true);
        mockAuthor.setDeleted(false);
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:query")
    void testList_Success() throws Exception {
        // Given
        List<LabAchievementAuthorEntity> authors = Arrays.asList(mockAuthor);
        when(authorService.getAuthorsByAchievementId(1L)).thenReturn(authors);

        // When & Then
        mockMvc.perform(get("/lab/achievements/1/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("张三"))
                .andExpect(jsonPath("$.data[0].authorOrder").value(1));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:edit")
    void testAdd_InternalAuthor_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);
        when(authorService.isAuthorOrderExists(eq(1L), eq(1), isNull())).thenReturn(false);
        when(authorService.save(any(LabAchievementAuthorEntity.class))).thenReturn(true);

        String requestBody = """
            {
                "userId": 5,
                "authorOrder": 1,
                "isCorresponding": false,
                "role": "第一作者",
                "visible": true
            }
            """;

        // When & Then
        mockMvc.perform(post("/lab/achievements/1/authors")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:edit")
    void testAdd_ExternalAuthor_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);
        when(authorService.isAuthorOrderExists(eq(1L), eq(2), isNull())).thenReturn(false);
        when(authorService.save(any(LabAchievementAuthorEntity.class))).thenReturn(true);

        String requestBody = """
            {
                "name": "John Doe",
                "nameEn": "John Doe",
                "affiliation": "MIT",
                "authorOrder": 2,
                "isCorresponding": true,
                "role": "通讯作者",
                "visible": true
            }
            """;

        // When & Then
        mockMvc.perform(post("/lab/achievements/1/authors")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:edit")
    void testAdd_DuplicateOrder_BadRequest() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);
        when(authorService.isAuthorOrderExists(eq(1L), eq(1), isNull())).thenReturn(true);

        String requestBody = """
            {
                "userId": 5,
                "authorOrder": 1,
                "isCorresponding": false,
                "role": "第一作者",
                "visible": true
            }
            """;

        // When & Then
        mockMvc.perform(post("/lab/achievements/1/authors")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:edit")
    void testUpdate_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);
        when(authorService.getById(1L)).thenReturn(mockAuthor);
        when(authorService.isAuthorOrderExists(eq(1L), eq(2), eq(1L))).thenReturn(false);
        when(authorService.updateById(any(LabAchievementAuthorEntity.class))).thenReturn(true);

        String requestBody = """
            {
                "affiliation": "MIT CSAIL",
                "authorOrder": 2,
                "isCorresponding": true,
                "role": "通讯作者"
            }
            """;

        // When & Then
        mockMvc.perform(put("/lab/achievements/1/authors/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:edit")
    void testDelete_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);
        when(authorService.getById(1L)).thenReturn(mockAuthor);
        when(authorService.updateById(any(LabAchievementAuthorEntity.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/lab/achievements/1/authors/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:edit")
    void testReorder_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);
        when(authorService.getById(1L)).thenReturn(mockAuthor);
        when(authorService.isAuthorOrderExists(eq(1L), eq(3), eq(1L))).thenReturn(false);
        when(authorService.updateById(any(LabAchievementAuthorEntity.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/lab/achievements/1/authors/1/reorder")
                .with(csrf())
                .param("newOrder", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @WithMockUser(authorities = "lab:achievement:edit")
    void testToggleVisibility_Success() throws Exception {
        // Given
        when(labUserPermissionChecker.getCurrentLabUser()).thenReturn(mockUser);
        when(labUserPermissionChecker.isAdmin()).thenReturn(true);
        when(authorService.getById(1L)).thenReturn(mockAuthor);
        when(authorService.updateById(any(LabAchievementAuthorEntity.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(put("/lab/achievements/1/authors/1/visibility")
                .with(csrf())
                .param("visible", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void testList_WithoutPermission_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/lab/achievements/1/authors"))
                .andExpect(status().isForbidden());
    }
}
