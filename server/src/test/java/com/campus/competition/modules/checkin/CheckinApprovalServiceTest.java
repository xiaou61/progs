package com.campus.competition.modules.checkin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.campus.competition.modules.checkin.model.CheckInCommand;
import com.campus.competition.modules.checkin.model.CheckinReviewCommand;
import com.campus.competition.modules.checkin.service.CheckinService;
import com.campus.competition.modules.competition.model.PublishCompetitionCommand;
import com.campus.competition.modules.competition.service.CompetitionService;
import com.campus.competition.modules.registration.model.RegisterCompetitionCommand;
import com.campus.competition.modules.registration.model.RegistrationAttendanceCommand;
import com.campus.competition.modules.registration.service.RegistrationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class CheckinApprovalServiceTest {

  @Test
  void shouldSubmitPendingApplicationAndApproveIntoPresent() {
    CompetitionService competitionService = new CompetitionService();
    RegistrationService registrationService = new RegistrationService(competitionService);
    CheckinService checkinService = new CheckinService(competitionService, registrationService);
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1001L,
      "签到审批赛",
      "用于验证签到申请后再由老师确认",
      LocalDateTime.now().minusDays(1),
      LocalDateTime.now().plusHours(2),
      LocalDateTime.now().minusHours(1),
      LocalDateTime.now().plusHours(2),
      40
    ));
    Long registrationId = registrationService.register(new RegisterCompetitionCommand(competitionId, 2001L));

    boolean checked = checkinService.checkIn(new CheckInCommand(competitionId, 2001L, "QRCODE"));

    assertTrue(checked);
    assertEquals("PENDING", checkinService.listByCompetition(competitionId).get(0).status());
    assertEquals("PENDING", registrationService.findByCompetitionAndUser(competitionId, 2001L).attendanceStatus());

    boolean reviewed = checkinService.reviewCheckinByRegistration(
      registrationId,
      new CheckinReviewCommand("APPROVED", null)
    );

    assertTrue(reviewed);
    assertEquals("APPROVED", checkinService.listByCompetition(competitionId).get(0).status());
    assertEquals("PRESENT", registrationService.findByCompetitionAndUser(competitionId, 2001L).attendanceStatus());
  }

  @Test
  void shouldRejectApplicationAndAllowResubmit() {
    CompetitionService competitionService = new CompetitionService();
    RegistrationService registrationService = new RegistrationService(competitionService);
    CheckinService checkinService = new CheckinService(competitionService, registrationService);
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1002L,
      "签到驳回赛",
      "用于验证驳回后允许再次提交",
      LocalDateTime.now().minusDays(1),
      LocalDateTime.now().plusHours(2),
      LocalDateTime.now().minusHours(1),
      LocalDateTime.now().plusHours(2),
      40
    ));
    Long registrationId = registrationService.register(new RegisterCompetitionCommand(competitionId, 2002L));

    checkinService.checkIn(new CheckInCommand(competitionId, 2002L, "QRCODE"));
    boolean reviewed = checkinService.reviewCheckinByRegistration(
      registrationId,
      new CheckinReviewCommand("REJECTED", "未到现场签到点")
    );

    assertTrue(reviewed);
    assertEquals("REJECTED", checkinService.listByCompetition(competitionId).get(0).status());
    assertEquals("未到现场签到点", checkinService.listByCompetition(competitionId).get(0).reviewRemark());
    assertEquals("PENDING", registrationService.findByCompetitionAndUser(competitionId, 2002L).attendanceStatus());

    assertTrue(checkinService.checkIn(new CheckInCommand(competitionId, 2002L, "QRCODE")));
    assertEquals("PENDING", checkinService.listByCompetition(competitionId).get(0).status());
  }

  @Test
  void shouldSyncRejectedStateWhenTeacherMarksAbsent() {
    CompetitionService competitionService = new CompetitionService();
    RegistrationService registrationService = new RegistrationService(competitionService);
    CheckinService checkinService = new CheckinService(competitionService, registrationService);
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1003L,
      "签到缺席赛",
      "用于验证老师标记缺席会同步拒绝签到申请",
      LocalDateTime.now().minusDays(1),
      LocalDateTime.now().plusHours(2),
      LocalDateTime.now().minusHours(1),
      LocalDateTime.now().plusHours(2),
      40
    ));
    Long registrationId = registrationService.register(new RegisterCompetitionCommand(competitionId, 2003L));

    checkinService.checkIn(new CheckInCommand(competitionId, 2003L, "QRCODE"));
    registrationService.markAttendance(registrationId, new RegistrationAttendanceCommand("ABSENT"));
    checkinService.syncWithAttendance(registrationId, "ABSENT");

    assertEquals("ABSENT", registrationService.findByCompetitionAndUser(competitionId, 2003L).attendanceStatus());
    assertEquals("REJECTED", checkinService.listByCompetition(competitionId).get(0).status());
  }

  @Test
  void shouldRequireReasonWhenRejectingApplication() {
    CompetitionService competitionService = new CompetitionService();
    RegistrationService registrationService = new RegistrationService(competitionService);
    CheckinService checkinService = new CheckinService(competitionService, registrationService);
    Long competitionId = competitionService.publish(new PublishCompetitionCommand(
      1004L,
      "签到校验赛",
      "用于验证驳回原因必填",
      LocalDateTime.now().minusDays(1),
      LocalDateTime.now().plusHours(2),
      LocalDateTime.now().minusHours(1),
      LocalDateTime.now().plusHours(2),
      40
    ));
    Long registrationId = registrationService.register(new RegisterCompetitionCommand(competitionId, 2004L));

    checkinService.checkIn(new CheckInCommand(competitionId, 2004L, "QRCODE"));

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
      checkinService.reviewCheckinByRegistration(registrationId, new CheckinReviewCommand("REJECTED", ""))
    );

    assertEquals("驳回原因不能为空", exception.getMessage());
  }
}
