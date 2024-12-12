package net.revature.project1.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "post")
public class Post {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "post_parent")
    private Post postParent;

    @Column(name = "user_id", nullable = false)
    private AppUser userId;

    private String comment;

    @Column(name = "image_path")
    private String imagePath;

    @Column(name = "video_path")
    private String videoPath;

    @Column(name = "post_at", nullable = false)
    private Timestamp postAt;

    @ManyToMany
    @JoinTable(
            name = "post_like",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<AppUser> likes = new HashSet<>();

    public Post(AppUser userId, String comment) {
        this.userId = userId;
        this.comment = comment;
    }

    public Post(AppUser userId, String comment, String imagePath, String videoPath) {
        this.userId = userId;
        this.imagePath = imagePath;
        this.videoPath = videoPath;
    }
}
