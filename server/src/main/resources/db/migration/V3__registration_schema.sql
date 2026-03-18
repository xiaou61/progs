create table cmp_registration (
  id bigint primary key auto_increment,
  competition_id bigint not null,
  user_id bigint not null,
  status varchar(16) not null default 'REGISTERED',
  created_at datetime not null default current_timestamp
);

