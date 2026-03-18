create table cmp_competition (
  id bigint primary key auto_increment,
  organizer_id bigint not null,
  title varchar(128) not null,
  description text not null,
  signup_start_at datetime not null,
  signup_end_at datetime not null,
  start_at datetime not null,
  end_at datetime not null,
  quota int not null,
  status varchar(16) not null default 'PUBLISHED',
  created_at datetime not null default current_timestamp
);

