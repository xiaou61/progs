create table cmp_checkin (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  user_id bigint not null,
  method varchar(16) not null,
  checked_at datetime not null
);

create table cmp_submission (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  user_id bigint not null,
  file_url varchar(255) not null,
  version_no int not null default 1,
  submitted_at datetime not null
);

