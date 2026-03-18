package com.campus.competition.modules.checkin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.competition.modules.checkin.persistence.CheckinEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CheckinMapper extends BaseMapper<CheckinEntity> {
}
