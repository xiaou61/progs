package com.campus.competition.modules.common.exception;

import com.campus.competition.modules.common.model.ApiResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
    return ApiResponse.failure(400, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    return ApiResponse.failure(400, "参数校验失败");
  }

  @ExceptionHandler(Exception.class)
  public ApiResponse<Void> handleException(Exception ex) {
    return ApiResponse.failure(500, "服务器内部错误");
  }
}
