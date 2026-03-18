create index idx_pts_record_user_biz_created
  on pts_record (user_id, biz_type, created_at);

create index idx_cmp_registration_user_status
  on cmp_registration (user_id, status);

create index idx_cmp_submission_user_competition
  on cmp_submission (user_id, competition_id);

