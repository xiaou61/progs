package com.campus.competition.modules.auth.persistence;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sys_user")
public class UserEntity {

  @TableId(type = IdType.AUTO)
  private Long id;
  private String studentNo;
  private String realName;
  private String phone;
  private String roleCode;
  private String passwordHash;
  private String status;
  private Boolean violationMarked;
  private String violationReason;
  private String avatarUrl;
  private String campusName;
  private String gradeName;
  private String majorName;
  private String departmentName;
  private String bio;
  private Boolean notifyResult;
  private Boolean notifyPoints;
  private Boolean allowPrivateMessage;
  private Boolean publicCompetition;
  private Boolean publicPoints;
  private Boolean publicSubmission;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getStudentNo() {
    return studentNo;
  }

  public void setStudentNo(String studentNo) {
    this.studentNo = studentNo;
  }

  public String getRealName() {
    return realName;
  }

  public void setRealName(String realName) {
    this.realName = realName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getRoleCode() {
    return roleCode;
  }

  public void setRoleCode(String roleCode) {
    this.roleCode = roleCode;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Boolean getViolationMarked() {
    return violationMarked;
  }

  public void setViolationMarked(Boolean violationMarked) {
    this.violationMarked = violationMarked;
  }

  public String getViolationReason() {
    return violationReason;
  }

  public void setViolationReason(String violationReason) {
    this.violationReason = violationReason;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getCampusName() {
    return campusName;
  }

  public void setCampusName(String campusName) {
    this.campusName = campusName;
  }

  public String getGradeName() {
    return gradeName;
  }

  public void setGradeName(String gradeName) {
    this.gradeName = gradeName;
  }

  public String getMajorName() {
    return majorName;
  }

  public void setMajorName(String majorName) {
    this.majorName = majorName;
  }

  public String getDepartmentName() {
    return departmentName;
  }

  public void setDepartmentName(String departmentName) {
    this.departmentName = departmentName;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public Boolean getNotifyResult() {
    return notifyResult;
  }

  public void setNotifyResult(Boolean notifyResult) {
    this.notifyResult = notifyResult;
  }

  public Boolean getNotifyPoints() {
    return notifyPoints;
  }

  public void setNotifyPoints(Boolean notifyPoints) {
    this.notifyPoints = notifyPoints;
  }

  public Boolean getAllowPrivateMessage() {
    return allowPrivateMessage;
  }

  public void setAllowPrivateMessage(Boolean allowPrivateMessage) {
    this.allowPrivateMessage = allowPrivateMessage;
  }

  public Boolean getPublicCompetition() {
    return publicCompetition;
  }

  public void setPublicCompetition(Boolean publicCompetition) {
    this.publicCompetition = publicCompetition;
  }

  public Boolean getPublicPoints() {
    return publicPoints;
  }

  public void setPublicPoints(Boolean publicPoints) {
    this.publicPoints = publicPoints;
  }

  public Boolean getPublicSubmission() {
    return publicSubmission;
  }

  public void setPublicSubmission(Boolean publicSubmission) {
    this.publicSubmission = publicSubmission;
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
