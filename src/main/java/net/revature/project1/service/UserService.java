package net.revature.project1.service;

import net.revature.project1.dto.UserDto;
import net.revature.project1.entity.AppUser;
import net.revature.project1.enumerator.UserEnum;
import net.revature.project1.repository.UserRepo;
import net.revature.project1.result.UserResult;
import net.revature.project1.utils.EmailPassRequirementsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private UserRepo userRepo;

    @Autowired
    public UserService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    /**
     * Returns a user DTO of the information that is need to display an accocunt information.
     * @param id Take in an id that will be searched for the user information.
     * @return A {@code UserResult} object that contains information about service status, and DTO.
     */
    public UserResult getUser(Long id){
        Optional<AppUser> optionalAppUser = userRepo.findById(id);
        if(optionalAppUser.isEmpty()){
            return new UserResult(UserEnum.UNKNOWN_USER, "Unknown user id.", null);
        }

        AppUser user = optionalAppUser.get();
        UserDto userDto = new UserDto(
                user.getUsername(), user.getDisplayName(),
                user.getProfilePic(), user.getBiography(),
                user.getFollower().size(), user.getFollowing().size(),
                user.getCreatedAt()
        );

        return new UserResult(UserEnum.SUCCESS, "Successfully got user profile!", userDto);
    }

    /**
     * Used to update the user email.
     *
     * @param id Take an ID to find the user and update it.
     * @param user Take in user object with the new email.
     * @return Returns an enum whether it was successful or not.
     */
    public UserEnum updateEmail(Long id, AppUser user, String oldEmail){
        boolean checkAuth = checkAuthorization(oldEmail);
        if(!checkAuth){
            return UserEnum.UNAUTHORIZED;
        }

        if(!EmailPassRequirementsUtils.isValidEmail(user.getEmail())){
            return UserEnum.INVALID_EMAIL_FORMAT;
        }

        if(userRepo.existsByEmail(user.getEmail())){
           return UserEnum.INVALID_EMAIL_FORMAT;
        }

        Optional<AppUser> getUser = userRepo.findById(id);
        if(getUser.isEmpty()){
            return UserEnum.UNKNOWN;
        }

        AppUser checkUser = getUser.get();
        checkUser.setEmail(user.getEmail());
        userRepo.save(checkUser);

        return UserEnum.SUCCESS;
    }

    /**
     * Used to change the user handler name.
     * @param id Take in a user id to find the user.
     * @param user Take in a user object to be used to change name.
     * @return UserEnum based on the status of the service.
     */
    public UserEnum updateUsername(Long id, AppUser user){
        boolean checkAuth = checkAuthorization();
        if(!checkAuth){
            return UserEnum.UNAUTHORIZED;
        }

        Optional<AppUser> userOptional = userRepo.findById(id);
        if(userOptional.isEmpty()){
            return UserEnum.UNKNOWN;
        }

        AppUser newUser = userOptional.get();
        newUser.setUsername(user.getUsername());
        userRepo.save(newUser);

        return UserEnum.SUCCESS;
    }

    /**
     * Used to change the user display name.
     * @param id Take in a user id to find the user.
     * @param appUser Take in a user object to be used to change name.
     * @return UserEnum based on the status of the service.
     */
    public UserEnum updateDisplayName(Long id, AppUser appUser){
        boolean checkAuth = checkAuthorization();
        if(!checkAuth){
            return UserEnum.UNAUTHORIZED;
        }

        Optional<AppUser> userOptional = userRepo.findById(id);
        if(userOptional.isEmpty()){
            return UserEnum.UNKNOWN;
        }

        AppUser newUser = userOptional.get();
        newUser.setDisplayName(appUser.getDisplayName());
        userRepo.save(newUser);

        return UserEnum.SUCCESS;
    }

    /**
     * Used to change the user biography.
     * @param id Take in a user id to find the user.
     * @param appUser Take in a user object to be used to change name.
     * @return UserEnum based on the status of the service.
     */
    public UserEnum updateBiography(Long id, AppUser appUser){
        boolean checkAuth = checkAuthorization();
        if(!checkAuth){
            return UserEnum.UNAUTHORIZED;
        }

        Optional<AppUser> userOptional = userRepo.findById(id);
        if(userOptional.isEmpty()){
            return UserEnum.UNKNOWN;
        }

        AppUser newUser = userOptional.get();
        newUser.setBiography(appUser.getBiography());
        userRepo.save(newUser);

        return UserEnum.SUCCESS;
    }
/*
    public UserEnum followNewUser(){

    }
*/
    /**
     * Used to check the authorization status if they can make the changes.
     *
     * This would obviously have a better verification system in place to prevent anyone from just changing
     * the important just because they certain information, but we don't have time to make sure verification
     * code can be sent.
     *
     * @param oldEmail Take in old email to compared from user details.
     * @return Return {@code True} if the user can make changes, or {@code False} if they cannot make changes.
     */
    public boolean checkAuthorization(String oldEmail){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()){
            return false;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername().equals(oldEmail);
    }

    public boolean checkAuthorization(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    /**
     * Used to get check if an email already exist.
     * @param email Take in a {@code String} with the email.
     * @return A {@code boolean} of true that it exist, or false it doesn't.
     */
    public boolean existsByEmail(String email){
        return userRepo.existsByEmail(email);
    }

    public boolean existsByUsername(String username){
        return userRepo.existsByUsername(username);
    }

    public Optional<AppUser> findAppUserByEmail(String email){
        return userRepo.findAppUserByEmail(email);
    }
}