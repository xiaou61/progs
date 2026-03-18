package com.campus.competition.modules.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.competition.modules.auth.persistence.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
