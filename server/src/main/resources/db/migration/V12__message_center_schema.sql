create table sys_message (
  id bigint primary key auto_increment,
  user_id bigint not null,
  title varchar(100) not null,
  content varchar(1000) not null,
  biz_type varchar(32) null,
  biz_id bigint null,
  read_flag boolean not null default false,
  created_at datetime not null default current_timestamp
);

create table msg_conversation (
  id bigint primary key auto_increment,
  owner_user_id bigint not null,
  conversation_type varchar(16) not null,
  peer_user_id bigint null,
  competition_id bigint null,
  title varchar(100) not null,
  last_message varchar(1000) null,
  last_message_at datetime not null default current_timestamp,
  unread_count int not null default 0,
  is_pinned boolean not null default false,
  is_muted boolean not null default false,
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp
);

create unique index uk_msg_conversation_private
  on msg_conversation (owner_user_id, conversation_type, peer_user_id);

create unique index uk_msg_conversation_group
  on msg_conversation (owner_user_id, conversation_type, competition_id);

create index idx_msg_conversation_owner
  on msg_conversation (owner_user_id, last_message_at);

create table msg_message (
  id bigint primary key auto_increment,
  conversation_type varchar(16) not null,
  sender_user_id bigint not null,
  receiver_user_id bigint null,
  competition_id bigint null,
  content varchar(1000) not null,
  created_at datetime not null default current_timestamp
);

create index idx_msg_message_private
  on msg_message (conversation_type, sender_user_id, receiver_user_id, created_at);

create index idx_msg_message_group
  on msg_message (conversation_type, competition_id, created_at);

