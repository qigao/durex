CREATE
    TABLE
        music(
            id VARCHAR(128) NOT NULL,
            title VARCHAR(255) NOT NULL,
            description VARCHAR(1024),
            duration INT,
            sample_rate VARCHAR(8),
            bit_rate INT,
            channels INT DEFAULT 2,
            music_type INT DEFAULT 0 NOT NULL,
            artist_id VARCHAR(255),
            cover_id VARCHAR(128),
            play_id VARCHAR(128) NOT NULL,
            lyric_id VARCHAR(128),
            create_time DATETIME,
            update_time DATETIME,
            delete_time DATETIME,
            deleted_flag INT DEFAULT 0,
            PRIMARY KEY(id)
        ) engine = InnoDB DEFAULT charset = utf8;

CREATE
    TABLE
        playlist(
            id VARCHAR(128) NOT NULL,
            description VARCHAR(1024),
            title VARCHAR(255),
            cover_id VARCHAR(128),
            create_time DATETIME,
            update_time DATETIME,
            delete_time DATETIME,
            deleted_flag INT DEFAULT 0,
            PRIMARY KEY(id)
        ) engine = InnoDB DEFAULT charset = utf8;

CREATE
    TABLE
        playlist_music(
            music_id VARCHAR(128) NOT NULL,
            playlist_id VARCHAR(128) NOT NULL,
            music_order INT,
            PRIMARY KEY(
                music_id,
                playlist_id
            )
        ) engine = InnoDB DEFAULT charset = utf8;

CREATE
    TABLE
        creator_playlist(
            playlist_id VARCHAR(128) NOT NULL,
            creator_id VARCHAR(128) NOT NULL,
            deleted_flag INT DEFAULT 0,
            PRIMARY KEY(
                playlist_id,
                creator_id
            )
        ) engine = InnoDB DEFAULT charset = utf8;

ALTER TABLE
    playlist_music ADD CONSTRAINT FK7ij69bixwcqsxgmuaq5fohiwl FOREIGN KEY(music_id) REFERENCES music(id);

ALTER TABLE
    playlist_music ADD CONSTRAINT FKg6v4c3et2lt7vf9lwkfn8fi15 FOREIGN KEY(playlist_id) REFERENCES playlist(id);

ALTER TABLE
    creator_playlist ADD CONSTRAINT FKRktfdXNlcl9wbGF5bGlzdA FOREIGN KEY(playlist_id) REFERENCES playlist(id);
