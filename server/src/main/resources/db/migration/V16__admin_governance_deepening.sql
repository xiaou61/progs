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

