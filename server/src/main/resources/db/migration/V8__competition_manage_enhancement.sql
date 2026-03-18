alter table cmp_competition add column is_recommended boolean not null default false;
alter table cmp_competition add column is_pinned boolean not null default false;
alter table cmp_competition add column updated_at datetime not null default current_timestamp;

