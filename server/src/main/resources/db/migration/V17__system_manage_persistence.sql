alter table org_campus add column updated_at datetime null;

insert into org_campus (campus_code, campus_name, status, created_at, updated_at)
select 'MAIN', '主校区', 'ENABLED', current_timestamp, current_timestamp
where not exists (
  select 1 from org_campus where campus_code = 'MAIN'
);

insert into org_campus (campus_code, campus_name, status, created_at, updated_at)
select 'EAST', '东校区', 'ENABLED', current_timestamp, current_timestamp
where not exists (
  select 1 from org_campus where campus_code = 'EAST'
);

update org_campus
set updated_at = created_at
where updated_at is null;

create table sys_banner (
  id bigint primary key auto_increment,
  title varchar(128) not null,
  status varchar(16) not null default 'ENABLED',
  jump_path varchar(255) not null,
  created_at datetime not null default current_timestamp,
  updated_at datetime null
);

insert into sys_banner (id, title, status, jump_path, created_at, updated_at)
select 1, '春季比赛季主视觉', 'ENABLED', '/pages/competition/list/index', current_timestamp, current_timestamp
where not exists (
  select 1 from sys_banner where id = 1
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

insert into sys_platform_config (
  id,
  platform_name,
  mvp_phase,
  points_enabled,
  submission_reupload_enabled,
  created_at,
  updated_at
)
select 1, '校园师生比赛管理平台', 'Phase 1', true, true, current_timestamp, current_timestamp
where not exists (
  select 1 from sys_platform_config where id = 1
);
