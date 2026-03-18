alter table msg_conversation
  add column cleared_at datetime null;

create index idx_msg_conversation_owner_cleared
  on msg_conversation (owner_user_id, cleared_at);

