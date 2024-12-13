package net.revature.project1.service;

import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.AuthResult;
import net.revature.project1.repository.AuthRepo;
import net.revature.project1.utils.EmailPassRequirementsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private AuthRepo authRepo;

    @Autowired
    public AuthService(AuthRepo authRepo){
        this.authRepo = authRepo;
    }

    /**
     * Create a new account for the user.
     * @param user Take in a user object to process information.
     * @return {@code AuthResult} with the status of the interaction.
     */
    @Transactional
    public AuthResult registration(AppUser user){
        if(!EmailPassRequirementsUtils.isValidEmail(user.getEmail()) ||
                !EmailPassRequirementsUtils.isValidPassword(user.getPassword())){
            return AuthResult.INVALID;
        }

        boolean isEmailAvailable = authRepo.existByEmail(user.getEmail());
        if(isEmailAvailable){
            return AuthResult.EMAIL_TAKEN;
        }

        boolean isUserAvailable = authRepo.existByUsername(user.getUsername());
        if(isUserAvailable){
            return AuthResult.USERNAME_TAKEN;
        }
        authRepo.save(user);

        return AuthResult.CREATED;
    }
}
