package com.campus.competition.modules.user.service;

import com.campus.competition.modules.auth.model.UserSummary;
import com.campus.competition.modules.user.model.AssignUserRoleCommand;
import com.campus.competition.modules.user.model.FreezeUserCommand;
import com.campus.competition.modules.user.model.ResetPasswordCommand;
import com.campus.competition.modules.user.model.ViolationGovernanceCommand;
import java.util.List;

public interface AdminUserService {

  List<UserSummary> listUsers();

  boolean freeze(Long userId, FreezeUserCommand command);

  boolean unfreeze(Long userId);

  boolean resetPassword(Long userId, ResetPasswordCommand command);

  UserSummary assignRole(Long userId, AssignUserRoleCommand command);

  boolean governViolation(Long userId, ViolationGovernanceCommand command);
}
