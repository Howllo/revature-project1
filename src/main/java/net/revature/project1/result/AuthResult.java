package net.revature.project1.result;

import lombok.Getter;
import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.AuthEnum;

@Getter
public class AuthResult {
    final private AuthEnum result;
    final private AppUser appUser;
    final private String token;

    public AuthResult(AuthEnum authEnum, AppUser appUser, String token){
        this.result = authEnum;
        this.appUser = appUser;
        this.token = token;
    }
}
