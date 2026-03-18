package com.campus.competition.modules.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.competition.modules.message.persistence.ConversationEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<ConversationEntity> {
}
