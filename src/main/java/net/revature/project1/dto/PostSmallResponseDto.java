package net.revature.project1.dto;

import java.sql.Timestamp;

public record PostSmallResponseDto(Long id, Long parentPost, String username, String displayName,
                                   String imagePath, String videoPath, Boolean postEdit, Timestamp postAt) {}
