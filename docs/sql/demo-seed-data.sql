-- 基础演示数据 SQL
-- 适用场景：
-- 1. 已执行项目中的 Flyway migration，表结构已经存在
-- 2. 需要补齐演示账号、内置角色、基础系统数据
-- 3. 数据库为 MySQL 8.x

-- 统一演示密码：
-- 明文：Abcd1234
-- BCrypt：$2b$12$Jn/LTfTr3NVhnCIp2F0pg.C8VcgTi.iMkaaK3K1zGqd8S7qXJO7a2

start transaction;

-- 内置角色
insert into sys_role (
  role_code,
  role_name,
  description,
  permission_codes,
  built_in,
  status,
  created_at,
  updated_at
)
select
  'STUDENT',
  '学生',
  '浏览比赛、报名参赛、签到上传、查看结果和积分。',
  'COMPETITION_VIEW,REGISTRATION_CREATE,CHECKIN_SUBMIT,SUBMISSION_UPLOAD,RESULT_VIEW,POINTS_VIEW,MESSAGE_USE',
  true,
  'ENABLED',
  now(),
  now()
from dual
where not exists (
  select 1 from sys_role where role_code = 'STUDENT'
);

insert into sys_role (
  role_code,
  role_name,
  description,
  permission_codes,
  built_in,
  status,
  created_at,
  updated_at
)
select
  'TEACHER',
  '老师',
  '发布比赛、管理报名、组织评审和发布结果。',
  'COMPETITION_VIEW,COMPETITION_MANAGE,REGISTRATION_MANAGE,REVIEW_MANAGE,SCORE_PUBLISH,RESULT_VIEW,MESSAGE_USE',
  true,
  'ENABLED',
  now(),
  now()
from dual
where not exists (
  select 1 from sys_role where role_code = 'TEACHER'
);

insert into sys_role (
  role_code,
  role_name,
  description,
  permission_codes,
  built_in,
  status,
  created_at,
  updated_at
)
select
  'ADMIN',
  '管理员',
  '负责用户治理、角色权限、系统配置和全局运营。',
  'USER_MANAGE,ROLE_MANAGE,CAMPUS_MANAGE,COMPETITION_MANAGE,REVIEW_MANAGE,SYSTEM_MANAGE,LOG_VIEW',
  true,
  'ENABLED',
  now(),
  now()
from dual
where not exists (
  select 1 from sys_role where role_code = 'ADMIN'
);

-- 演示账号
insert into sys_user (
  student_no,
  real_name,
  phone,
  role_code,
  password_hash,
  status,
  violation_marked,
  violation_reason,
  avatar_url,
  campus_name,
  grade_name,
  major_name,
  department_name,
  bio,
  notify_result,
  notify_points,
  allow_private_message,
  public_competition,
  public_points,
  public_submission,
  created_at,
  updated_at
)
select
  'T20260001',
  '王老师',
  '13800000011',
  'TEACHER',
  '$2b$12$Jn/LTfTr3NVhnCIp2F0pg.C8VcgTi.iMkaaK3K1zGqd8S7qXJO7a2',
  'ENABLED',
  false,
  null,
  null,
  '主校区',
  null,
  null,
  '信息工程学院',
  '教师演示账号',
  true,
  true,
  true,
  true,
  true,
  true,
  now(),
  now()
from dual
where not exists (
  select 1 from sys_user where student_no = 'T20260001'
);

insert into sys_user (
  student_no,
  real_name,
  phone,
  role_code,
  password_hash,
  status,
  violation_marked,
  violation_reason,
  avatar_url,
  campus_name,
  grade_name,
  major_name,
  department_name,
  bio,
  notify_result,
  notify_points,
  allow_private_message,
  public_competition,
  public_points,
  public_submission,
  created_at,
  updated_at
)
select
  'S20260001',
  '张同学',
  '13800000001',
  'STUDENT',
  '$2b$12$Jn/LTfTr3NVhnCIp2F0pg.C8VcgTi.iMkaaK3K1zGqd8S7qXJO7a2',
  'ENABLED',
  false,
  null,
  null,
  '主校区',
  '2026级',
  '软件工程',
  '信息工程学院',
  '学生演示账号',
  true,
  true,
  true,
  true,
  true,
  true,
  now(),
  now()
from dual
where not exists (
  select 1 from sys_user where student_no = 'S20260001'
);

insert into sys_user (
  student_no,
  real_name,
  phone,
  role_code,
  password_hash,
  status,
  violation_marked,
  violation_reason,
  avatar_url,
  campus_name,
  grade_name,
  major_name,
  department_name,
  bio,
  notify_result,
  notify_points,
  allow_private_message,
  public_competition,
  public_points,
  public_submission,
  created_at,
  updated_at
)
select
  'A20260001',
  '系统管理员',
  '13800000099',
  'ADMIN',
  '$2b$12$Jn/LTfTr3NVhnCIp2F0pg.C8VcgTi.iMkaaK3K1zGqd8S7qXJO7a2',
  'ENABLED',
  false,
  null,
  null,
  '主校区',
  null,
  null,
  '平台运营中心',
  '后台管理员演示账号',
  true,
  true,
  true,
  true,
  true,
  true,
  now(),
  now()
from dual
where not exists (
  select 1 from sys_user where student_no = 'A20260001'
);

-- 基础校区
insert into org_campus (
  campus_code,
  campus_name,
  status,
  created_at,
  updated_at
)
select 'MAIN', '主校区', 'ENABLED', now(), now()
from dual
where not exists (
  select 1 from org_campus where campus_code = 'MAIN'
);

insert into org_campus (
  campus_code,
  campus_name,
  status,
  created_at,
  updated_at
)
select 'EAST', '东校区', 'ENABLED', now(), now()
from dual
where not exists (
  select 1 from org_campus where campus_code = 'EAST'
);

-- 基础轮播图
insert into sys_banner (
  id,
  title,
  status,
  jump_path,
  created_at,
  updated_at
)
select 1, '春季比赛季主视觉', 'ENABLED', '/pages/competition/list/index', now(), now()
from dual
where not exists (
  select 1 from sys_banner where id = 1
);

-- 系统配置
insert into sys_platform_config (
  id,
  platform_name,
  mvp_phase,
  points_enabled,
  submission_reupload_enabled,
  created_at,
  updated_at
)
select 1, '校园师生比赛管理平台', 'Phase 1', true, true, now(), now()
from dual
where not exists (
  select 1 from sys_platform_config where id = 1
);

commit;
