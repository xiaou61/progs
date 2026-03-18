package com.campus.competition.modules.common.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(value = "com.campus.competition.modules", annotationClass = Mapper.class)
public class MybatisPlusConfig {
}
