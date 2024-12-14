package net.revature.project1.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDto {
    private String message;
    private String username;
    private String displayName;
    private String profilePicture;

    public AuthResponseDto(String message, String username, String displayName, String profilePicture) {
        this.message = message;
        this.username = username;
        this.displayName = displayName;
        this.profilePicture = profilePicture;
    }
}
