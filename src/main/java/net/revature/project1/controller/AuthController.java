package net.revature.project1.controller;

import jakarta.servlet.http.HttpSession;
import net.revature.project1.dto.AuthResponseDto;
import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.AuthEnum;
import net.revature.project1.result.AuthResult;
import net.revature.project1.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    final private AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AppUser user, HttpSession session){
        AuthResult result = authService.registration(user);
        AppUser returnedUser = result.getAppUser();

        return switch (result.getResult()) {
            case CREATED, SUCCESS -> {
                session.setAttribute("email", user.getEmail());
                session.setAttribute("user", user.getUsername());

                yield ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponseDto("Successfully created a account.",
                            returnedUser.getUsername(),
                            null,
                            returnedUser.getProfilePic()
                    ));
            }
            case EMAIL_TAKEN -> ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Email already registered");
            case USERNAME_TAKEN -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already in used");
            case INVALID, INVALID_CREDENTIALS -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Bad email or password");
            case UNKNOWN -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error - An unexpected error occurred on the server. " +
                            "Please try again later");
        };
    }

    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestBody AppUser user, HttpSession session){
        AuthResult authResult = authService.login(user);
        AuthEnum result = authResult.getResult();
        AppUser returnUser = authResult.getAppUser();

        return switch(result) {
            case CREATED, SUCCESS -> {
                session.setAttribute("email", user.getEmail());
                session.setAttribute("user", authResult.getAppUser().getUsername());
                yield ResponseEntity.status(HttpStatus.OK)
                        .body(new AuthResponseDto("Login successful!",
                                returnUser.getUsername(),
                                returnUser.getDisplayName(),
                                returnUser.getProfilePic()
                        ));
                }
                case INVALID, INVALID_CREDENTIALS -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid email or password. Please try again.");
                case UNKNOWN, USERNAME_TAKEN, EMAIL_TAKEN -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Internal Server Error - An unexpected error occurred on the server. " +
                                "Please try again later");
        };
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session){
        session.invalidate();
        return ResponseEntity.ok("Successfully logged out");
    }
}

