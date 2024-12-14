package net.revature.project1.result;

import lombok.Getter;
import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.AuthEnum;

@Getter
public class AuthResult {
    final private AuthEnum result;
    final private AppUser appUser;

    public AuthResult(AuthEnum authEnum, AppUser appUser){
        this.result = authEnum;
        this.appUser = appUser;
    }
}
