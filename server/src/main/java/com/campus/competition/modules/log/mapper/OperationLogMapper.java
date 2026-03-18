package com.campus.competition.modules.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.competition.modules.log.persistence.OperationLogEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogEntity> {
}
