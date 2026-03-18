-- 师生比赛管理平台数据库汇总 SQL
-- 说明：本文件按版本顺序汇总 server/src/main/resources/db/migration 下的 V1 ~ V16 迁移脚本。
-- 用途：便于整体查看当前数据库结构与演进结果，不作为 Flyway 迁移直接执行。

-- ===== V1__init_base_schema.sql =====
create table sys_user (
  id bigint primary key auto_increment,
  student_no varchar(32) not null unique,
  real_name varchar(64) not null,
  phone varchar(20) not null,
  role_code varchar(32) not null,
  password_hash varchar(128) not null,
  status varchar(16) not null default 'ENABLED',
  created_at datetime not null default current_timestamp
);

create table org_campus (
  id bigint primary key auto_increment,
  campus_code varchar(32) not null unique,
  campus_name varchar(64) not null,
  status varchar(16) not null default 'ENABLED',
  created_at datetime not null default current_timestamp
);


-- ===== V2__competition_schema.sql =====
create table cmp_competition (
  id bigint primary key auto_increment,
  organizer_id bigint not null,
  title varchar(128) not null,
  description text not null,
  signup_start_at datetime not null,
  signup_end_at datetime not null,
  start_at datetime not null,
  end_at datetime not null,
  quota int not null,
  status varchar(16) not null default 'PUBLISHED',
  created_at datetime not null default current_timestamp
);


-- ===== V3__registration_schema.sql =====
create table cmp_registration (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  user_id bigint not null,
  status varchar(16) not null default 'REGISTERED',
  created_at datetime not null default current_timestamp
);


-- ===== V4__checkin_submission_schema.sql =====
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


-- ===== V5__review_score_points_schema.sql =====
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


-- ===== V6__score_points_column.sql =====
alter table cmp_score add column points int not null default 0;


-- ===== V7__user_profile_settings.sql =====
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


-- ===== V8__competition_manage_enhancement.sql =====
alter table cmp_competition add column is_recommended boolean not null default false;
alter table cmp_competition add column is_pinned boolean not null default false;
alter table cmp_competition add column updated_at datetime not null default current_timestamp;


-- ===== V9__registration_manage_enhancement.sql =====
alter table cmp_registration add column attendance_status varchar(16) not null default 'PENDING';
alter table cmp_registration add column remark varchar(255) null;
alter table cmp_registration add column updated_at datetime not null default current_timestamp;


-- ===== V10__review_certificate_enhancement.sql =====
alter table cmp_review_task add column student_id bigint null;
alter table cmp_review_task add column review_comment varchar(500) null;
alter table cmp_review_task add column suggested_score int null;
alter table cmp_review_task add column reviewed_at datetime null;
alter table cmp_review_task add column updated_at datetime not null default current_timestamp;

alter table cmp_score add column reviewer_name varchar(64) null;
alter table cmp_score add column review_comment varchar(500) null;
alter table cmp_score add column certificate_no varchar(64) null;
alter table cmp_score add column certificate_title varchar(128) null;


-- ===== V11__daily_task_overview.sql =====
create index idx_pts_record_user_biz_created
  on pts_record (user_id, biz_type, created_at);

create index idx_cmp_registration_user_status
  on cmp_registration (user_id, status);

create index idx_cmp_submission_user_competition
  on cmp_submission (user_id, competition_id);


-- ===== V12__message_center_schema.sql =====
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


-- ===== V13__role_governance_schema.sql =====
create table sys_role (
  id bigint primary key auto_increment,
  role_code varchar(32) not null unique,
  role_name varchar(64) not null,
  description varchar(255) null,
  permission_codes varchar(1000) not null,
  built_in boolean not null default false,
  status varchar(16) not null default 'ENABLED',
  created_at datetime not null default current_timestamp,
  updated_at datetime not null default current_timestamp
);


-- ===== V14__message_conversation_manage.sql =====
alter table msg_conversation
  add column cleared_at datetime null;

create index idx_msg_conversation_owner_cleared
  on msg_conversation (owner_user_id, cleared_at);


-- ===== V15__local_audit_schema.sql =====
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


-- ===== V16__admin_governance_deepening.sql =====
alter table sys_user
  add column violation_marked boolean not null default false;

alter table sys_user
  add column violation_reason varchar(255) null;

create table sys_operation_log (
  id bigint primary key auto_increment,
  operator_name varchar(64) not null,
  action varchar(64) not null,
  target varchar(128) not null,
  detail varchar(500) null,
  created_at datetime not null default current_timestamp
);

create index idx_sys_operation_log_action
  on sys_operation_log (action, created_at, id);

create index idx_sys_operation_log_operator
  on sys_operation_log (operator_name, created_at, id);


