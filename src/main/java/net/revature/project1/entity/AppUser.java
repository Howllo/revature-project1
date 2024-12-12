package net.revature.project1.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name="app_user")
public class AppUser {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String display_name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    private String biography;

    @Column(nullable = false)
    private Timestamp created_at;

    @OneToMany(mappedBy = "app_user")
    private Set<PostLike> likes = new HashSet<>();

    public AppUser() {}

    public AppUser(String display_name, String email, String username, String password) {
        this.display_name = display_name;
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
