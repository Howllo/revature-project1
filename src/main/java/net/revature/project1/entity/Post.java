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

    @ManyToOne
    @JoinColumn(name = "parent_post")
    private Post postParent;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

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
    private final Set<AppUser> likes = new HashSet<>();

    public Post() {}

    public Post(AppUser user, String comment) {
        this.user = user;
        this.comment = comment;
        this.postAt = new Timestamp(System.currentTimeMillis());
    }

    public Post(AppUser user, String comment, String imagePath, String videoPath) {
        this.user = user;
        this.comment = comment;
        this.imagePath = imagePath;
        this.videoPath = videoPath;
        this.postAt = new Timestamp(System.currentTimeMillis());
    }
}
