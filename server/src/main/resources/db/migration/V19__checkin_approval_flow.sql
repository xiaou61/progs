alter table cmp_checkin
  add column status varchar(16) not null default 'PENDING';

alter table cmp_checkin
  add column review_remark varchar(255) null;

alter table cmp_checkin
  add column reviewed_at datetime null;

create unique index uk_cmp_checkin_competition_user on cmp_checkin(competition_id, user_id);
