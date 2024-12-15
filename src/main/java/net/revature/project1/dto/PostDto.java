package net.revature.project1.dto;

import java.sql.Timestamp;

public record PostDto(Long id, Long parentPost, Long userId, String username, String displayName,
                      String imagePath, String videoPath, Boolean postEdit, Timestamp postAt) {}
