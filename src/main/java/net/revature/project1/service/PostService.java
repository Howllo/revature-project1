package net.revature.project1.service;

import net.revature.project1.dto.PostFeedRequest;
import net.revature.project1.dto.PostFeedResponse;
import net.revature.project1.dto.PostResponseDto;
import net.revature.project1.dto.PostSmallResponseDto;
import net.revature.project1.entity.AppUser;
import net.revature.project1.entity.Post;
import net.revature.project1.enumerator.PostEnum;
import net.revature.project1.repository.PostRepo;
import net.revature.project1.result.PostResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    final private PostRepo postRepo;
    final private UserService userService;
    final private FileService fileService;

    @Autowired
    public PostService(PostRepo postRepo, UserService userService, FileService fileService) {
        this.postRepo = postRepo;
        this.userService = userService;
        this.fileService = fileService;
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
    public PostResult createPost(Post post) {

        if(post.getComment() == null && post.getMedia() == null) {
            return new PostResult(PostEnum.INVALID_POST, "Post must have a comment, " +
                 "image, or video.", null);
        }

        if(post.getComment() != null && post.getComment().length() > 255) {
            return new PostResult(PostEnum.INVALID_COMMENT, "Comment is too long.", null);
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
    public PostResult updatePost(Long id, Post post) {

        Optional<Post> postOptional = postRepo.findById(id);
        if(postOptional.isEmpty()) {
            return new PostResult(PostEnum.INVALID_POST, "Post does not exist.", null);
        }
        Post postToUpdate = postOptional.get();


        if(Timestamp.valueOf(post.getPostAt().toLocalDateTime()).getTime() + 300000 < System.currentTimeMillis()) {
            return new PostResult(PostEnum.INVALID_POST, "Post cannot be edited after 5 minutes.", null);
        }

        postToUpdate.setComment(post.getComment());
        postToUpdate.setMedia(post.getMedia());

        if(Timestamp.valueOf(post.getPostAt().toLocalDateTime()).getTime() + 60001 < System.currentTimeMillis()) {
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
    public PostEnum deletePost(Long id) {

        Optional<Post> post = postRepo.findById(id);
        if(post.isEmpty()) {
            return PostEnum.INVALID_POST;
        }

        postRepo.delete(post.get());
        return PostEnum.SUCCESS;
    }

    /**
     * Allows user to like a post.
     * @param id The id of the post to be liked.
     * @param userId The id of the user liking the post.
     * @return The status of the like.
     */
    public PostEnum likePost(Long id, Long userId) {

        Optional<Post> post = postRepo.findById(id);
        if(post.isEmpty()) {
            return PostEnum.INVALID_POST;
        }
        Post postToUpdate = post.get();

        Optional<AppUser> user = userService.findUserById(userId);
        if(user.isEmpty()) {
            return PostEnum.INVALID_USER;
        }

        postToUpdate.getLikes().add(user.get());
        postRepo.save(postToUpdate);
        return PostEnum.SUCCESS;
    }

    /**
     * Get the comments of a post.
     * @param postId The id of the post.
     * @return The comments of the post.
     */
    public List<PostResponseDto> getComments(Long postId) {
        return null;
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
}
