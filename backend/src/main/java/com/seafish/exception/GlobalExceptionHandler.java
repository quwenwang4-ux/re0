package com.seafish.exception;

import com.seafish.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBusinessException(
            BusinessException exception
    ) {
        return ApiResponse.failure(
                exception.getCode(),
                exception.getMessage()
        );
    }
    @ExceptionHandler(
            MethodArgumentNotValidException.class
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        String message = "请求参数不合法";

        List<FieldError> fieldErrors =
                exception
                        .getBindingResult()
                        .getFieldErrors();

        if (!fieldErrors.isEmpty()) {
            message = fieldErrors
                    .get(0)
                    .getDefaultMessage();
        }

        return ApiResponse.failure(
                40001,
                message
        );
    }
}