# MVP 数据表说明

## 账号与组织

| 表名 | 说明 |
|---|---|
| `sys_user` | 用户基础信息 |
| `sys_role` | 角色定义 |
| `sys_user_role` | 用户角色关系 |
| `sys_campus` | 校区信息 |

## 比赛主流程

| 表名 | 说明 |
|---|---|
| `cmp_competition` | 比赛主表 |
| `cmp_registration` | 比赛报名表 |
| `cmp_checkin` | 比赛签到表 |
| `cmp_submission` | 作品提交表 |

## 评审与结果

| 表名 | 说明 |
|---|---|
| `cmp_review_task` | 评审任务表 |
| `cmp_score` | 比赛成绩与奖项表 |

## 积分

| 表名 | 说明 |
|---|---|
| `pts_account` | 用户积分账户 |
| `pts_record` | 积分流水 |

## 当前实现说明

- 当前后端以最小可运行内存态服务为主，迁移脚本已为后续切换到 MySQL 持久化预留表结构。
- 下一阶段会补实体、Mapper、Service 持久化实现，以及基础索引和唯一约束。
