package net.revature.project1.service;

import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.AuthEnum;
import net.revature.project1.repository.AuthRepo;
import net.revature.project1.result.AuthResult;
import net.revature.project1.utils.EmailPassRequirementsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {
    final private AuthRepo authRepo;
    final private UserService userService;
    final private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(AuthRepo authRepo, PasswordEncoder passwordEncoder, UserService userService){
        this.authRepo = authRepo;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
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
            return new AuthResult(AuthEnum.INVALID, user);
        }

        boolean isEmailAvailable = userService.existsByEmail(user.getEmail());
        if(isEmailAvailable){
            return new AuthResult(AuthEnum.EMAIL_TAKEN, user);
        }

        boolean isUserAvailable = userService.existsByUsername(user.getUsername());
        if(isUserAvailable){
            return new AuthResult(AuthEnum.USERNAME_TAKEN, user);
        }

        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);

        AppUser returnUser = authRepo.save(user);

        return new AuthResult(AuthEnum.CREATED, returnUser);
    }

    /**
     * Used to log into the system with just a AppUser data.
     * @param user Take in a data to be processed.
     * @return {@code AuthResult} of status of the auth service layer.
     */
    public AuthResult login(AppUser user){
        if(user.getEmail().isEmpty() || user.getPassword().isEmpty()){
            return new AuthResult(AuthEnum.INVALID_CREDENTIALS, user);
        }

        if(!EmailPassRequirementsUtils.isValidEmail(user.getEmail())){
            return new AuthResult(AuthEnum.INVALID_CREDENTIALS, user);
        }

        Optional<AppUser> optionalAppUser = userService.findAppUserByEmail(user.getEmail());
        if(optionalAppUser.isEmpty()){
            return new AuthResult(AuthEnum.INVALID_CREDENTIALS, user);
        }

        AppUser checkUser = optionalAppUser.get();
        if(!passwordEncoder.matches(user.getPassword(), checkUser.getPassword())){
            return new AuthResult(AuthEnum.INVALID_CREDENTIALS, user);
        }


        return new AuthResult(AuthEnum.SUCCESS, checkUser);
    }
}
