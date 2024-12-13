package net.revature.project1.controller;

import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.AuthResult;
import net.revature.project1.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    final private AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @RequestMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUser user){
        AuthResult result = authService.registration(user);
        record UserResponse(String username, String displayName){ }

        return switch (result) {
            case CREATED, SUCCESS -> ResponseEntity.status(HttpStatus.CREATED)
                    .body(new UserResponse(user.getUsername(), user.getDisplayName()));
            case EMAIL_TAKEN -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email already registered");
            case USERNAME_TAKEN -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already in used");
            case INVALID, INVALID_CREDENTIALS -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Bad email or password");
            case UNKNOWN -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error - An unexpected error occurred on the server. Please try again later");
        };
    }
/*
    @RequestMapping("/login")
    public ResponseEntity<?> login(@RequestBody AppUser user){
        AuthResult result = authService.login(user);
    }

    @RequestMapping("/refresh")
    public  ResponseEntity<?> refresh(@CookieValue("refreshToken") String refreshToken){

    }*/
}
