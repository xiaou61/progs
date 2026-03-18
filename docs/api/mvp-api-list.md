# MVP 接口清单

## 公共与账号

| 端 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 公共 | `GET` | `/api/public/health` | 健康检查 |
| 小程序 | `POST` | `/api/app/auth/register` | 用户注册 |
| 小程序 | `POST` | `/api/app/auth/login` | 用户登录 |

## 比赛主流程

| 端 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 后台 | `POST` | `/api/admin/competitions` | 发布比赛 |
| 后台 | `GET` | `/api/admin/competitions` | 查询比赛列表 |
| 小程序 | `GET` | `/api/app/competitions` | 查询公开比赛列表 |
| 小程序 | `GET` | `/api/app/competitions/{competitionId}` | 查询比赛详情 |
| 小程序 | `POST` | `/api/app/registrations` | 提交报名 |
| 小程序 | `GET` | `/api/app/registrations/competition/{competitionId}` | 查询比赛报名名单 |
| 小程序 | `POST` | `/api/app/checkins` | 提交签到 |
| 小程序 | `GET` | `/api/app/checkins/competition/{competitionId}` | 查询签到记录 |
| 小程序 | `POST` | `/api/app/submissions` | 提交作品 |
| 小程序 | `GET` | `/api/app/submissions/competition/{competitionId}` | 查询作品列表 |

## 评审结果与积分

| 端 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 后台 | `GET` | `/api/admin/reviews/tasks?competitionId=` | 查询待评审任务 |
| 后台 | `POST` | `/api/admin/scores/publish` | 发布比赛结果 |
| 后台 | `GET` | `/api/admin/scores/competition/{competitionId}` | 查询比赛成绩 |
| 小程序 | `GET` | `/api/app/results/competition/{competitionId}` | 查询比赛结果 |
| 小程序 | `GET` | `/api/app/results/student/{studentId}` | 查询个人结果与积分流水 |
| 小程序 | `GET` | `/api/app/results/points?userId=` | 查询积分账户 |

## 后台运营

| 端 | 方法 | 路径 | 说明 |
|---|---|---|---|
| 后台 | `GET` | `/api/admin/banners` | 查询轮播图配置 |
| 后台 | `GET` | `/api/admin/configs` | 查询系统配置 |
| 后台 | `GET` | `/api/admin/logs` | 查询操作日志 |
