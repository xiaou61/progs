package com.campus.competition.modules.system.persistence;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sys_platform_config")
public class PlatformConfigEntity {

  @TableId
  private Long id;
  private String platformName;
  private String mvpPhase;
  private Boolean pointsEnabled;
  private Boolean submissionReuploadEnabled;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getPlatformName() {
    return platformName;
  }

  public void setPlatformName(String platformName) {
    this.platformName = platformName;
  }

  public String getMvpPhase() {
    return mvpPhase;
  }

  public void setMvpPhase(String mvpPhase) {
    this.mvpPhase = mvpPhase;
  }

  public Boolean getPointsEnabled() {
    return pointsEnabled;
  }

  public void setPointsEnabled(Boolean pointsEnabled) {
    this.pointsEnabled = pointsEnabled;
  }

  public Boolean getSubmissionReuploadEnabled() {
    return submissionReuploadEnabled;
  }

  public void setSubmissionReuploadEnabled(Boolean submissionReuploadEnabled) {
    this.submissionReuploadEnabled = submissionReuploadEnabled;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
