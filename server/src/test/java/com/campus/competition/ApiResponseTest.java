package com.campus.competition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.campus.competition.modules.common.model.ApiResponse;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

  @Test
  void successShouldContainCodeZero() {
    ApiResponse<String> response = ApiResponse.success("ok");
    assertEquals(0, response.getCode());
    assertEquals("ok", response.getData());
  }
}
