package net.revature.project1.dto;

public class UserSearchDto {
    final String username;
    final String displayName;
    final String profileImageUrl;

    public UserSearchDto(String username, String displayName, String profileImageUrl) {
        this.username = username;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
    }
}
