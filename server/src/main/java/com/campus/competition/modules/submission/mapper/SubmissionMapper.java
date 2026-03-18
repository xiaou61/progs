package com.campus.competition.modules.submission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.competition.modules.submission.persistence.SubmissionEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubmissionMapper extends BaseMapper<SubmissionEntity> {
}
