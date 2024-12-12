package net.revature.project1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import net.revature.project1.utils.PostLikeId;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "post_like")
public class PostLike {
    @EmbeddedId
    private PostLikeId id;

    @ManyToOne
    @MapsId("user_id")
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne
    @MapsId("post_id")
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "post_at", nullable = false)
    private Timestamp postAt;
}
