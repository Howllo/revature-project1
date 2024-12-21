package net.revature.project1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
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

    @Column(name= "post_edit")
    private boolean postEdited;

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

    public Long getId() {
        return id;
    }

    public Post getPostParent() {
        return postParent;
    }

    public AppUser getUser() {
        return user;
    }

    public String getComment() {
        return comment;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public Timestamp getPostAt() {
        return postAt;
    }

    public boolean isPostEdited() {
        return postEdited;
    }

    public Set<AppUser> getLikes() {
        return likes;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setPostEdited(boolean postEdited) {
        this.postEdited = postEdited;
    }
}
