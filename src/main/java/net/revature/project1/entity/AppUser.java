package net.revature.project1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name="app_user")
public class AppUser {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "profile_pic")
    private String profilePic;

    @Column(name = "banner_pic")
    private String bannerPic;

    private String biography;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @ManyToMany
    @JoinTable(
            name = "follower_following",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name ="following_id")
    )
    private final Set<AppUser> follower = new HashSet<>();

    @ManyToMany(mappedBy = "follower")
    private final Set<AppUser> following = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_friend",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name ="friend_id", referencedColumnName = "id")
    )
    private final Set<AppUser> initiatedFriendships  = new HashSet<>();

    @ManyToMany(mappedBy = "initiatedFriendships")
    private final Set<AppUser> receivedFriendships  = new HashSet<>();

    @ManyToMany(mappedBy = "likes")
    private final Set<Post> likedPosts = new HashSet<>();

    public AppUser() {}

    public AppUser(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
