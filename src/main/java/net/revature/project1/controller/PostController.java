package net.revature.project1.controller;

import net.revature.project1.dto.PostDto;
import net.revature.project1.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/post")
public class PostController {
    final private PostService postService;

    @Autowired
    private PostController(PostService postService){
        this.postService = postService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long id){
        return ResponseEntity.ok(postService.getPost(id));
    }
}

