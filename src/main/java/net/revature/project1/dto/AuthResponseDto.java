package net.revature.project1.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponseDto {
    public String message;
    public String username;
    public String displayName;
    public String profilePicture;
    public String token;

    public AuthResponseDto(String message, String username, String displayName, String profilePicture, String token) {
        this.message = message;
        this.username = username;
        this.displayName = displayName;
        this.profilePicture = profilePicture;
        this.token = token;
    }
}
