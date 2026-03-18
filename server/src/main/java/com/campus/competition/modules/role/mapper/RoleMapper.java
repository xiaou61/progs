package com.campus.competition.modules.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.competition.modules.role.persistence.RoleEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {
}
