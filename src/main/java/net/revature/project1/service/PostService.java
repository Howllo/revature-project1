package net.revature.project1.service;

import io.jsonwebtoken.Claims;
import net.revature.project1.dto.*;
import net.revature.project1.entity.AppUser;
import net.revature.project1.entity.Post;
import net.revature.project1.enumerator.PostEnum;
import net.revature.project1.repository.PostRepo;
import net.revature.project1.result.PostResult;
import net.revature.project1.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class PostService {
    final private PostRepo postRepo;
    final private UserService userService;
    final private FileService fileService;
    final private JwtTokenUtil jwtTokenUtil;

    @Autowired
    public PostService(PostRepo postRepo, UserService userService, FileService fileService, JwtTokenUtil jwtTokenUtil) {
        this.postRepo = postRepo;
        this.userService = userService;
        this.fileService = fileService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    // Good luck! I don't have Redis.
    public List<PostResponseDto> getAllPosts() {
        List<Post> getAll = postRepo.findAll();
        List<PostResponseDto> posts = new ArrayList<>();
        for (Post post : getAll) {
            Long parentPost = 0L;

            if(post.getPostParent() == null){
                parentPost = -1L;
            } else {
                parentPost = post.getPostParent().getId();
            }

            posts.add(new PostResponseDto(
                    post.getId(),
                    parentPost,
                    post.getUser().getId(),
                    post.getUser().getUsername(),
                    post.getUser().getDisplayName(),
                    post.getComment(),
                    post.getMedia(),
                    post.isPostEdited(),
                    post.getPostAt(),
                    post.getLikes().size(),
                    (long) post.getComment().length()
            ));
        }
        return posts;
    }

    /**
     * Get a post by its id.
     * @param id The id of the post.
     * @return The post.
     */
    public PostSmallResponseDto getPost(Long id) {
        Optional<PostSmallResponseDto> postDto = postRepo.getUserPost(id);
        return postDto.orElse(null);
    }

    /**
     * Poorly written, but I don't have time to create a better implementation.
     * @param request The request for the feed.
     *                 Contains the user id, the last post id, and the chunk size.
     * @param chunkSize The size of the chunk.
     * @return The chunk of posts.
     */
    public PostFeedResponse getUserFeed(PostFeedRequest request, Integer chunkSize){
        List<Post> posts = postRepo.getFollowingPostsChunk(request.userId(), request.lastPostId(), chunkSize);
        List<Post> postsToBeDisplayed = new ArrayList<>();

        for(Post post : posts){
            if (!request.seenPostId().contains(post.getId())) {
                postsToBeDisplayed.add(post);
                request.seenPostId().add(post.getId());

                if (postsToBeDisplayed.size() >= chunkSize) {
                    break;
                }
            }
        }

        return new PostFeedResponse(postsToBeDisplayed, request.seenPostId());
    }

    /**
    * Create a post.
    * @param post The post to be created.
    * @return The created post.
    */
    public PostResult createPost(Post post, String token) {
        if(post.getComment() == null && post.getMedia() == null) {
            return new PostResult(PostEnum.INVALID_POST, "Post must have a comment, " +
                 "image, or video.", null);
        }

        if(post.getComment() != null && post.getComment().length() > 255) {
            return new PostResult(PostEnum.INVALID_COMMENT, "Comment is too long.", null);
        }

        boolean isValid = isValidToken(token, post);
        if(!isValid){
            return new PostResult(PostEnum.INVALID_POST, "User and post are not the same", null);
        }

        if(post.getMedia() != null && !post.getMedia().isEmpty() && !post.getMedia().contains("youtube")){
            try{
                fileService.createFile(post);
            } catch (IOException e) {
                return new PostResult(PostEnum.INVALID_POST, "File could not be created.", null);
            }
        }

        post.setPostAt(Timestamp.from(Instant.now()));
        postRepo.save(post);

        return new PostResult(PostEnum.SUCCESS, null, getPostResponseDto(post));
    }

    /**
     * Update a post.
     * @param id The id of the post to be updated.
     * @param post The post to be updated.
     * @return The updated version of the post.
     */
    public PostResult updatePost(Long id, PostUpdateDto post, String token) {

        Optional<Post> postOptional = postRepo.findById(id);
        if(postOptional.isEmpty()) {
            return new PostResult(PostEnum.INVALID_POST, "Post does not exist.", null);
        }
        Post postToUpdate = postOptional.get();

        try{
            if(Timestamp.valueOf(post.postAt().toLocalDateTime()).getTime() + 300000 < System.currentTimeMillis()) {
                return new PostResult(PostEnum.INVALID_POST, "Post cannot be edited after 5 minutes.", null);
            }
        } catch (NullPointerException e) {
            return new PostResult(PostEnum.INVALID_POST, "Post does not exist.", null);
        }

        postToUpdate.setComment(post.comment());

        if(Timestamp.valueOf(post.postAt().toLocalDateTime()).getTime() + 60001 < System.currentTimeMillis()) {
            postToUpdate.setPostEdited(true);
        }

        postRepo.save(postToUpdate);
        return new PostResult(PostEnum.SUCCESS, "Successfully updated post.",
                getPostResponseDto(postToUpdate));
    }

    /**
    * Delete a post.
    * @param id The id of the post to be deleted.
    * @return The deleted post.
    */
    public PostEnum deletePost(Long id, String token) {
        String username = jwtTokenUtil.getUsernameFromToken(token);

        Optional<Post> post = postRepo.findById(id);
        if(post.isEmpty()) {
            return PostEnum.INVALID_POST;
        }
        Post postToDelete = post.get();

        boolean isValid =  isValidToken(token, postToDelete);

        if(!isValid) {
            return PostEnum.UNAUTHORIZED;
        }

        postRepo.delete(postToDelete);
        return PostEnum.SUCCESS;
    }

    /**
     * Allows user to like a post.
     * @param id The id of the post to be liked.
     * @param userId The id of the user liking the post.
     * @return The status of the like.
     */
    public PostEnum likePost(Long id, Long userId, String token) {
        Optional<Post> post = postRepo.findById(id);
        if(post.isEmpty()) {
            return PostEnum.INVALID_POST;
        }
        Post postToUpdate = post.get();

        Optional<AppUser> optionalAppUser = userService.findUserById(userId);
        if(optionalAppUser.isEmpty()) {
            return PostEnum.INVALID_USER;
        }
        AppUser appUser = optionalAppUser.get();

        Optional<AppUser> optionalAppUser2 = getUser(token);
        if(optionalAppUser2.isEmpty()) {
            return PostEnum.INVALID_USER;
        }
        AppUser appUser2 = optionalAppUser2.get();

        if(!appUser2.getUsername().equals(appUser.getUsername())){
            return PostEnum.UNAUTHORIZED;
        }

        if(postToUpdate.getLikes().contains(appUser)) {
            postToUpdate.getLikes().remove(appUser);
            postRepo.save(postToUpdate);
            return PostEnum.SUCCESS_UNLIKED;
        } else {
            postToUpdate.getLikes().add(appUser);
            postRepo.save(postToUpdate);
            return PostEnum.SUCCESS;
        }
    }

    /**
     * Get the comments of a post.
     * @param postId The id of the post.
     * @return The comments of the post.
     */
    public List<PostResponseDto> getComments(Long postId) {
        return null;
    }

    public Integer returnTotalLikes(Long postId) {
        Optional<Post> optionalPost = postRepo.findById(postId);
        if(optionalPost.isEmpty()) {
            return null;
        }
        Post post = optionalPost.get();

        return post.getLikes().size();
    }

    public boolean doesUserLikeThisPost(Long postId, Long userId, String token) {
        Optional<Post> optionalPost = postRepo.findById(postId);
        if(optionalPost.isEmpty()) {
            return false;
        }
        Post post = optionalPost.get();
        Optional<AppUser> optionalAppUser = userService.findUserById(userId);
        if(optionalAppUser.isEmpty()) {
            return false;
        }
        AppUser appUser = optionalAppUser.get();

        Optional<AppUser> optionalAppUser2 = getUser(token);
        if(optionalAppUser2.isEmpty()) {
            return false;
        }
        AppUser appUser2 = optionalAppUser2.get();
        if(!appUser2.getUsername().equals(appUser.getUsername())){
            return false;
        }

        return post.getLikes().contains(appUser);
    }

    /**
     * Get a post response dto.
     * @param post Post object to be converted.
     * @return The post response dto.
     */
    PostResponseDto getPostResponseDto(Post post) {
        Long postParent = -1L;
        if(post.getPostParent() != null) {
            postParent = post.getPostParent().getId();
        }

        return new PostResponseDto(
                post.getId(),
                postParent,
                post.getUser().getId(),
                post.getUser().getUsername(),
                post.getUser().getDisplayName(),
                post.getComment(),
                post.getMedia(),
                post.isPostEdited(),
                post.getPostAt(),
                post.getLikes().size(),
                postRepo.getPostCommentNumber(post.getId())
        );
    }

    /**
     * Used to verify if the person that is posting is the same person that has the token.
     * @param token Take in a token to process the request.
     * @param post Take in a post to get the user id.
     * @return Return a {@code True} if the poster and user is same, and {@Code false} if the user is not
     * the same.
     */
    public boolean isValidToken(String token, Post post) {
        Optional<AppUser> optionalAppUser = getUser(token);
        if(optionalAppUser.isEmpty()){
            return false;
        }

        AppUser appUser = optionalAppUser.get();

        if(!Objects.equals(post.getUser().getId(), appUser.getId())){
            return false;
        }
        return true;
    }

    /**
     * Returns the user of a token if there is a username listed.
     * @param token Take in the JWT token to be processed.
     * @return The AppUser that is associated with the token.
     */
    private Optional<AppUser> getUser(String token) {
        return userService.findByUsername(jwtTokenUtil.getUsernameFromToken(token));
    }
}
