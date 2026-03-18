create table sys_violation_record (
  id bigint primary key auto_increment,
  scene varchar(32) not null,
  biz_id bigint null,
  user_id bigint null,
  reason varchar(255) not null,
  hit_words varchar(255) null,
  content_snippet varchar(500) not null,
  created_at datetime not null default current_timestamp
);

create index idx_sys_violation_record_created
  on sys_violation_record (created_at, id);

create index idx_sys_violation_record_user
  on sys_violation_record (user_id, created_at, id);

