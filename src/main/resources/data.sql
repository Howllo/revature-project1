DROP TABLE IF EXISTS post_like;
DROP TABLE IF EXISTS follow;
DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
    id INTEGER auto_increment,
    display_name VARCHAR(20) NOT NULL,
    email VARCHAR(50) NOT NULL,
    username VARCHAR(20) NOT NULL,
    password VARCHAR(100) NOT NULL,
    biography VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL
    PRIMARY KEY (id)
);

CREATE TABLE post (
    id INTEGER auto_increment,
    parent_post INTEGER DEFAULT NULL,
    user_id INTEGER NOT NULL,
    comment VARCHAR(255),
    image_path VARCHAR(255),
    video_path VARCHAR(255),
    post_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY(parent_post) REFERENCES post(id),
    FOREIGN KEY(user_id) REFERENCES app_user(id)
);

CREATE TABLE follow (
    follower_id INTEGER NOT NULL,
    following_id INTEGER NOT NULL,
    FOREIGN KEY (follower_id) REFERENCES app_user(id),
    FOREIGN KEY (following_id) REFERENCES app_user(id),
    PRIMARY KEY (follower_id, following_id)
);

CREATE TABLE post_like (
    post_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (post_id) REFERENCES post(id),
    FOREIGN KEY (user_id) REFERENCES app_user(id),
    PRIMARY KEY (post_id, user_id)
);