package net.revature.project1.controller;

import jakarta.servlet.http.HttpSession;
import net.revature.project1.dto.UserDto;
import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.UserEnum;
import net.revature.project1.result.UserResult;
import net.revature.project1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("api/v1/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    // This would be rate limited.
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserDto(@PathVariable Long id){
        UserResult userResult = userService.getUser(id);
        return switch (userResult.getResult()){
            case SUCCESS -> ResponseEntity.ok(userResult.getUserDto());
            case EMAIL_ALREADY_EXISTS, UNAUTHORIZED, INVALID_EMAIL_FORMAT -> null;
            case UNKNOWN_USER, UNKNOWN -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(userResult.getMessage());
        };
    }

    // This would be rate limited.
    @GetMapping("/{username}")
    public ResponseEntity<String> checkUsername(@PathVariable String string, HttpSession session) {
        boolean isAvailable = userService.existsByUsername(string);
        if(!isAvailable){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This username is already taken.");
        }
        return ResponseEntity.ok("This username is available.");
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<String> updateUserEmail(@PathVariable Long id,
                                                  @RequestBody AppUser appUser, HttpSession session) {
        UserEnum result = userService.updateEmail(id, appUser, session.getAttribute("email").toString());
        return switch (result){
            case SUCCESS -> {
                session.setAttribute("email", appUser.getEmail());
                yield ResponseEntity.ok("Successfully changed email!");
            }
            case UNAUTHORIZED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to do " +
                    "that.");
            case EMAIL_ALREADY_EXISTS -> ResponseEntity.status(HttpStatus.CONFLICT).body("That email is already in " +
                    "use.");
            case INVALID_EMAIL_FORMAT -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The email in which was" +
                    "provided does not meet the requirements.");
            case UNKNOWN,UNKNOWN_USER -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server" +
                    " Error - An unexpected error occurred on the server. Please try again later");
        };
    }

    @PutMapping("/{id}/username")
    public ResponseEntity<String> updateUsername(@PathVariable Long id,
                                                 @RequestBody AppUser appUser, HttpSession session) {
        UserEnum result = userService.updateDisplayName(id, appUser);
        return returnType(result, appUser, session);
    }

    @PutMapping("/{id}/display_name")
    public ResponseEntity<String> updateDisplayName(@PathVariable Long id,
                                                    @RequestBody AppUser appUser, HttpSession session) {
        UserEnum result = userService.updateDisplayName(id, appUser);
        return returnType(result, appUser, session);
    }

    @PutMapping("/{id}/biography")
    public ResponseEntity<String> updateBiography(@PathVariable Long id,
                                                  @RequestBody AppUser appUser, HttpSession session) {
        UserEnum result = userService.updateBiography(id, appUser);
        return returnType(result, appUser, session);
    }
/*
    @PostMapping("/{id}/follow/{user}")
    public ResponseEntity<String> followUser(@PathVariable Long id,
                                             @PathVariable Long user_id,
                                             HttpSession session) {

    }

    @DeleteMapping("/{id}/follow/{user}")
    public ResponseEntity<String> unfollowUser(@PathVariable Long id,
                                             @PathVariable Long user_id,
                                             HttpSession session) {

    }
*/
    private ResponseEntity<String> returnType(UserEnum result, AppUser appUser, HttpSession session){
        return switch (result){
            case SUCCESS -> {
                if(!appUser.getUsername().isEmpty()){
                    session.setAttribute("user", appUser.getUsername());
                }
                yield ResponseEntity.ok("Successfully update information.");
            }
            case UNAUTHORIZED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not authorized to do this.");
            case EMAIL_ALREADY_EXISTS -> ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use.");
            case INVALID_EMAIL_FORMAT -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This email fails to follow" +
                    "the email standard.");
            case UNKNOWN, UNKNOWN_USER -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server" +
                    " Error - An unexpected error occurred on the server. Please try again later");
        };
    }
}
