package com.campus.competition.modules.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.competition.modules.audit.persistence.ViolationRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ViolationRecordMapper extends BaseMapper<ViolationRecordEntity> {
}
