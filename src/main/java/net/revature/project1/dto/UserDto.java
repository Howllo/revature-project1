package net.revature.project1.dto;

import java.sql.Timestamp;

public record UserDto(String username, String displayName, String profileImageUrl, String biography,
                      Integer followerCount, Integer followingCount, Timestamp joinDate) { }
