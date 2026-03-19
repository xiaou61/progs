alter table cmp_competition add column participant_type varchar(32) not null default 'STUDENT_ONLY';
alter table cmp_competition add column advisor_teacher_id bigint null;

update cmp_competition
set advisor_teacher_id = organizer_id
where participant_type = 'STUDENT_ONLY'
  and advisor_teacher_id is null;
