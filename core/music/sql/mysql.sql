   create table music (
       id VARCHAR(128) not null,
        artist VARCHAR(255),
        channels INT default 2,
        coverUrl VARCHAR(1024),
        create_time DATETIME,
        description VARCHAR(1024),
        device VARCHAR(255),
        duration INT,
        editor VARCHAR(255),
        musicType INT default 0 not null,
        playUrl VARCHAR(1024) not null,
        sampleRate INT default 44,
        storyId VARCHAR(255),
        title VARCHAR(255) not null,
        update_time DATETIME,
        voiceName VARCHAR(255),
        primary key (id)
    ) engine=InnoDB
    create table playlist (
       id VARCHAR(128) not null,
        coverUrl VARCHAR(1024),
        create_time DATETIME,
        description VARCHAR(255),
        device VARCHAR(255),
        editor VARCHAR(255),
        title VARCHAR(255),
        update_time DATETIME,
        primary key (id)
    ) engine=InnoDB
    create table playlist_music (
       musicId varchar(255) not null,
        playlistId varchar(255) not null,
        musicOrder integer,
        primary key (musicId, playlistId)
    ) engine=InnoDB
    alter table playlist_music
       add constraint FK7ij69bixwcqsxgmuaq5fohiwl
       foreign key (musicId)
       references music (id)
    alter table playlist_music
       add constraint FKg6v4c3et2lt7vf9lwkfn8fi15
       foreign key (playlistId)
       references playlist (id)
