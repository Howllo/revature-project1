package net.revature.project1.dto;

import net.revature.project1.entity.Post;

import java.sql.Timestamp;

public record PostResponseDto(Long id, Post parentPost, Long userId, String username, String displayName,
                              String imagePath, String videoPath, Boolean postEdit, Timestamp postAt,
                              int likeCount, Long commentCount) {
}
