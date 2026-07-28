package com.seafish.exception;

import com.seafish.common.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTests {

    @Test
    void handlesBusinessException() {
        GlobalExceptionHandler handler =
                new GlobalExceptionHandler();

        BusinessException exception =
                new BusinessException(
                        40003,
                        "没有操作权限"
                );

        ApiResponse<Void> response =
                handler.handleBusinessException(exception);

        assertEquals(40003, response.getCode());
        assertEquals("没有操作权限", response.getMessage());
        assertNull(response.getData());
    }
}