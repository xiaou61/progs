alter table cmp_registration add column attendance_status varchar(16) not null default 'PENDING';
alter table cmp_registration add column remark varchar(255) null;
alter table cmp_registration add column updated_at datetime not null default current_timestamp;

