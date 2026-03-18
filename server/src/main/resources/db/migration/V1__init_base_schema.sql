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

