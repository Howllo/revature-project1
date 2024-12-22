package net.revature.project1.controller;

import jakarta.servlet.http.HttpSession;
import net.revature.project1.dto.PostFeedRequest;
import net.revature.project1.dto.PostFeedResponse;
import net.revature.project1.dto.PostResponseDto;
import net.revature.project1.dto.PostSmallResponseDto;
import net.revature.project1.entity.Post;
import net.revature.project1.enumerator.PostEnum;
import net.revature.project1.result.PostResult;
import net.revature.project1.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173/")
@RestController
@RequestMapping("api/v1/post")
public class PostController {
    final private PostService postService;
    final Integer CHUNK_SIZE = 100;

    @Autowired
    private PostController(PostService postService){
        this.postService = postService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostSmallResponseDto> getPost(@PathVariable Long id){
        return ResponseEntity.ok(postService.getPost(id));
    }

    @GetMapping("/{id}/feed")
    public ResponseEntity<PostFeedResponse> getUserFeed(@PathVariable Long id,
                                                        @RequestBody PostFeedRequest postFeedRequest,
                                                        HttpSession session){
        return ResponseEntity.ok(postService.getUserFeed(postFeedRequest, CHUNK_SIZE));
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<PostResponseDto>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getComments(id));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPost(@RequestBody Post post){
        PostResult postResult = postService.createPost(post);
        PostEnum result = postResult.postEnum();

        return switch(result){
            case SUCCESS -> ResponseEntity.ok(postResult.post());
            case INVALID_POST, INVALID_USER, INVALID_COMMENT -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(postResult.message());
            case UNAUTHORIZED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(postResult.message());
        };
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody Post post){
        PostResult postResult = postService.updatePost(id, post);
        PostEnum result = postResult.postEnum();

        return switch(result){
            case SUCCESS -> ResponseEntity.ok(postResult.post());
            case INVALID_POST, INVALID_USER, INVALID_COMMENT -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(postResult.message());
            case UNAUTHORIZED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(postResult.message());
        };
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id){
        PostEnum result = postService.deletePost(id);

        return switch(result){
            case SUCCESS -> ResponseEntity.ok("Successfully deleted post.");
            case INVALID_POST, INVALID_USER, INVALID_COMMENT -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid post.");
            case UNAUTHORIZED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Internal Server Error - An unexpected error occurred on the server. " +
                            "Please try again later");
        };
    }

    @PostMapping("/{id}/like/{userId}")
    public ResponseEntity<String> likePost(@PathVariable Long id,
                                           @PathVariable Long userId){
        PostEnum result = postService.likePost(id, userId);

        return switch(result){
            case SUCCESS -> ResponseEntity.ok("Successfully liked post.");
            case INVALID_POST, INVALID_USER, INVALID_COMMENT -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid post.");
            case UNAUTHORIZED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Internal Server Error - An unexpected error occurred on the server. " +
                            "Please try again later");
        };
    }
}

