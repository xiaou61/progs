-- 师生比赛管理平台数据库完整 SQL
-- 说明：本文件已将 V1 ~ V18 的最终结构合并为一份可直接执行的 MySQL 8 初始化脚本。

create database campus_competition
  character set utf8mb4
  collate utf8mb4_unicode_ci;

use campus_competition;

-- 账号与组织
create table sys_user (
  id bigint primary key auto_increment,
  student_no varchar(32) not null unique,
  real_name varchar(64) not null,
  phone varchar(20) not null,
  role_code varchar(32) not null,
  password_hash varchar(128) not null,
  status varchar(16) not null default 'ENABLED',
  created_at datetime not null default current_timestamp,
  avatar_url varchar(255) null,
  campus_name varchar(64) null,
  grade_name varchar(64) null,
  major_name varchar(64) null,
  department_name varchar(64) null,
  bio varchar(255) null,
  notify_result boolean not null default true,
  notify_points boolean not null default true,
  allow_private_message boolean not null default true,
  public_competition boolean not null default true,
  public_points boolean not null default true,
  public_submission boolean not null default true,
  updated_at datetime not null default current_timestamp,
  violation_marked boolean not null default false,
  violation_reason varchar(255) null
);

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

create table org_campus (
  id bigint primary key auto_increment,
  campus_code varchar(32) not null unique,
  campus_name varchar(64) not null,
  status varchar(16) not null default 'ENABLED',
  created_at datetime not null default current_timestamp,
  updated_at datetime null
);

create table sys_feedback (
  id bigint primary key auto_increment,
  user_id bigint not null,
  content varchar(1000) not null,
  image_urls varchar(1000) null,
  status varchar(16) not null default 'SUBMITTED',
  created_at datetime not null default current_timestamp
);

create table sys_operation_log (
  id bigint primary key auto_increment,
  operator_name varchar(64) not null,
  action varchar(64) not null,
  target varchar(128) not null,
  detail varchar(500) null,
  created_at datetime not null default current_timestamp,
  key idx_sys_operation_log_action (action, created_at, id),
  key idx_sys_operation_log_operator (operator_name, created_at, id)
);

-- 比赛主流程
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
  created_at datetime not null default current_timestamp,
  is_recommended boolean not null default false,
  is_pinned boolean not null default false,
  updated_at datetime not null default current_timestamp,
  participant_type varchar(32) not null default 'STUDENT_ONLY',
  advisor_teacher_id bigint null
);

create table cmp_registration (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  user_id bigint not null,
  status varchar(16) not null default 'REGISTERED',
  created_at datetime not null default current_timestamp,
  attendance_status varchar(16) not null default 'PENDING',
  remark varchar(255) null,
  updated_at datetime not null default current_timestamp,
  key idx_cmp_registration_user_status (user_id, status)
);

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
  submitted_at datetime not null,
  key idx_cmp_submission_user_competition (user_id, competition_id)
);

-- 评审与结果
create table cmp_review_task (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  submission_id bigint not null,
  reviewer_name varchar(64) not null,
  status varchar(16) not null,
  created_at datetime not null,
  student_id bigint null,
  review_comment varchar(500) null,
  suggested_score int null,
  reviewed_at datetime null,
  updated_at datetime not null default current_timestamp
);

create table cmp_score (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  user_id bigint not null,
  score int not null,
  rank_no int not null,
  award_name varchar(64) not null,
  published_at datetime not null,
  points int not null default 0,
  reviewer_name varchar(64) null,
  review_comment varchar(500) null,
  certificate_no varchar(64) null,
  certificate_title varchar(128) null
);

-- 积分
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
  biz_id bigint null,
  remark varchar(255) null,
  created_at datetime not null,
  key idx_pts_record_user_biz_created (user_id, biz_type, created_at)
);

-- 消息与沟通
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
  updated_at datetime not null default current_timestamp,
  cleared_at datetime null,
  unique key uk_msg_conversation_private (owner_user_id, conversation_type, peer_user_id),
  unique key uk_msg_conversation_group (owner_user_id, conversation_type, competition_id),
  key idx_msg_conversation_owner (owner_user_id, last_message_at),
  key idx_msg_conversation_owner_cleared (owner_user_id, cleared_at)
);

create table msg_message (
  id bigint primary key auto_increment,
  conversation_type varchar(16) not null,
  sender_user_id bigint not null,
  receiver_user_id bigint null,
  competition_id bigint null,
  content varchar(1000) not null,
  created_at datetime not null default current_timestamp,
  key idx_msg_message_private (conversation_type, sender_user_id, receiver_user_id, created_at),
  key idx_msg_message_group (conversation_type, competition_id, created_at)
);

-- 审核与系统配置
create table sys_violation_record (
  id bigint primary key auto_increment,
  scene varchar(32) not null,
  biz_id bigint null,
  user_id bigint null,
  reason varchar(255) not null,
  hit_words varchar(255) null,
  content_snippet varchar(500) not null,
  created_at datetime not null default current_timestamp,
  key idx_sys_violation_record_created (created_at, id),
  key idx_sys_violation_record_user (user_id, created_at, id)
);

create table sys_banner (
  id bigint primary key auto_increment,
  title varchar(128) not null,
  status varchar(16) not null default 'ENABLED',
  jump_path varchar(255) not null,
  created_at datetime not null default current_timestamp,
  updated_at datetime null
);

create table sys_platform_config (
  id bigint primary key,
  platform_name varchar(128) not null,
  mvp_phase varchar(64) not null,
  points_enabled boolean not null default true,
  submission_reupload_enabled boolean not null default true,
  created_at datetime not null default current_timestamp,
  updated_at datetime null
);
