package com.campus.competition.modules.points.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("pts_account")
public class PointsAccountEntity {

  @TableId(type = IdType.INPUT)
  private Long userId;
  private Integer availablePoints;
  private Integer totalPoints;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Integer getAvailablePoints() {
    return availablePoints;
  }

  public void setAvailablePoints(Integer availablePoints) {
    this.availablePoints = availablePoints;
  }

  public Integer getTotalPoints() {
    return totalPoints;
  }

  public void setTotalPoints(Integer totalPoints) {
    this.totalPoints = totalPoints;
  }
}
