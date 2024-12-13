package net.revature.project1.service;

import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.AuthResult;
import net.revature.project1.repository.AuthRepo;
import net.revature.project1.utils.EmailPassRequirementsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {
    final private AuthRepo authRepo;
    final private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(AuthRepo authRepo, PasswordEncoder passwordEncoder){
        this.authRepo = authRepo;
        this.passwordEncoder = passwordEncoder;
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

        boolean isEmailAvailable = authRepo.existsByEmail(user.getEmail());
        if(isEmailAvailable){
            return AuthResult.EMAIL_TAKEN;
        }

        boolean isUserAvailable = authRepo.existsByUsername(user.getUsername());
        if(isUserAvailable){
            return AuthResult.USERNAME_TAKEN;
        }

        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);

        authRepo.save(user);

        return AuthResult.CREATED;
    }

    /**
     * Used to log into the system with just a AppUser data.
     * @param user Take in a data to be processed.
     * @return {@code AuthResult} of status of the auth service layer.
     */
    public AuthResult login(AppUser user){
        if(user.getEmail().isEmpty() || user.getPassword().isEmpty()){
            return AuthResult.INVALID_CREDENTIALS;
        }

        if(!EmailPassRequirementsUtils.isValidEmail(user.getEmail())){
            return AuthResult.INVALID_CREDENTIALS;
        }

        Optional<AppUser> optionalAppUser = authRepo.findAppUserByEmail(user.getEmail());
        if(optionalAppUser.isEmpty()){
            return AuthResult.INVALID_CREDENTIALS;
        }

        AppUser checkUser = optionalAppUser.get();
        if(!passwordEncoder.matches(user.getPassword(), checkUser.getPassword())){
            return AuthResult.INVALID_CREDENTIALS;
        }

        return AuthResult.SUCCESS;
    }
}
