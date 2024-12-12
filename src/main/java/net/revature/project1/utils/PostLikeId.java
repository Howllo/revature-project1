package net.revature.project1.utils;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PostLikeId implements Serializable {
    private Long userID;
    private Long postID;

    public PostLikeId() {}

    public PostLikeId(Long userID, Long postID){
        this.userID = userID;
        this.postID = postID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PostLikeId that = (PostLikeId) o;
        return Objects.equals(userID, that.userID) && Objects.equals(postID, that.postID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, postID);
    }
}
