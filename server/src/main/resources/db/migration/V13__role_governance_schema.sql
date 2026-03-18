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

