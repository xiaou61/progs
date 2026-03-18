alter table sys_user add column avatar_url varchar(255) null;
alter table sys_user add column campus_name varchar(64) null;
alter table sys_user add column grade_name varchar(64) null;
alter table sys_user add column major_name varchar(64) null;
alter table sys_user add column department_name varchar(64) null;
alter table sys_user add column bio varchar(255) null;
alter table sys_user add column notify_result boolean not null default true;
alter table sys_user add column notify_points boolean not null default true;
alter table sys_user add column allow_private_message boolean not null default true;
alter table sys_user add column public_competition boolean not null default true;
alter table sys_user add column public_points boolean not null default true;
alter table sys_user add column public_submission boolean not null default true;
alter table sys_user add column updated_at datetime not null default current_timestamp;

create table sys_feedback (
  id bigint primary key auto_increment,
  user_id bigint not null,
  content varchar(1000) not null,
  image_urls varchar(1000) null,
  status varchar(16) not null default 'SUBMITTED',
  created_at datetime not null default current_timestamp
);

