alter table cmp_review_task add column student_id bigint null;
alter table cmp_review_task add column review_comment varchar(500) null;
alter table cmp_review_task add column suggested_score int null;
alter table cmp_review_task add column reviewed_at datetime null;
alter table cmp_review_task add column updated_at datetime not null default current_timestamp;

alter table cmp_score add column reviewer_name varchar(64) null;
alter table cmp_score add column review_comment varchar(500) null;
alter table cmp_score add column certificate_no varchar(64) null;
alter table cmp_score add column certificate_title varchar(128) null;

