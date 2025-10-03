-- drop table GuildSettings;
-- drop table UserSettings;

create table GuildSettings (guildId bigint not null, musicChannelId bigint, primary key (guildId));
create table UserSettings (userId bigint not null, playlistFromTrack bool, primary key (userId));