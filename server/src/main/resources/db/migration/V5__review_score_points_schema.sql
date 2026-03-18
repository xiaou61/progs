create table cmp_review_task (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  submission_id bigint not null,
  reviewer_name varchar(64) not null,
  status varchar(16) not null,
  created_at datetime not null
);

create table cmp_score (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  user_id bigint not null,
  score int not null,
  rank_no int not null,
  award_name varchar(64) not null,
  published_at datetime not null
);

create table pts_account (
  user_id bigint primary key,
  available_points int not null default 0,
  total_points int not null default 0
);

create table pts_record (
  id bigint primary key auto_increment,
  user_id bigint not null,
  change_amount int not null,
  biz_type varchar(32) not null,
  biz_id bigint,
  remark varchar(255),
  created_at datetime not null
);

