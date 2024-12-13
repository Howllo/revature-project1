package net.revature.project1.controller;

import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.AuthResult;
import net.revature.project1.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @RequestMapping("/register")
    public ResponseEntity<?> register(AppUser user){
        AuthResult result = authService.registration(user);

    }
}
