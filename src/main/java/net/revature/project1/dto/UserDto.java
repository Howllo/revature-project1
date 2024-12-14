package net.revature.project1.dto;

import java.sql.Timestamp;

public class UserDto {
    final String username;
    final String displayName;
    final String profileImageUrl;
    final String biography;
    final Integer followerCount;
    final Integer followingCount;
    final Timestamp joinDate;

    public UserDto(String username, String displayName, String profileImageUrl, String biography,
                   Integer followerCount, Integer followingCount, Timestamp joinDate) {
        this.username = username;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.biography = biography;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.joinDate = joinDate;
    }
}
