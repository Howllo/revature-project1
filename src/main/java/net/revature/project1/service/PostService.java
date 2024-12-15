package net.revature.project1.service;

import net.revature.project1.dto.PostDto;
import net.revature.project1.repository.PostRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostService {
    final private PostRepo postRepo;

    @Autowired
    public PostService(PostRepo postRepo){
        this.postRepo = postRepo;
    }

    /**
     * Get a post by its id.
     * @param id The id of the post.
     * @return The post.
     */
    public PostDto getPost(Long id) {
        Optional<PostDto> postDto = postRepo.getUserPost(id);
        return postDto.orElse(null);
    }


}
