package net.revature.project1.repository;

import net.revature.project1.dto.PostDto;
import net.revature.project1.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepo extends JpaRepository<Post, Long> {

    @Query("SELECT new net.revature.project1.dto.PostDto(p.id, p.parentPost, p.user.id, p.user.username, " +
            "p.user.displayName, p.imagePath, p.videoPath, p.postEdit, p.postAt) FROM Post p WHERE p.id = :id")
    Optional<PostDto> getUserPost(@Param("id") Long id);
}
