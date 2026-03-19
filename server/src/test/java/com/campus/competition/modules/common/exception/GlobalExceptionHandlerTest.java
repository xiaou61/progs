package com.campus.competition.modules.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.campus.competition.modules.common.model.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void shouldLogUnexpectedExceptionAndReturnGenericMessage(CapturedOutput output) {
    RuntimeException ex = new RuntimeException("数据库表不存在");

    ApiResponse<Void> response = handler.handleException(ex);

    assertThat(response.getCode()).isEqualTo(500);
    assertThat(response.getMessage()).isEqualTo("服务器内部错误");
    assertThat(output).contains("未处理异常");
    assertThat(output).contains("RuntimeException");
    assertThat(output).contains("数据库表不存在");
  }
}
