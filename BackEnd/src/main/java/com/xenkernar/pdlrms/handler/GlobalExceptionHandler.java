package com.xenkernar.pdlrms.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.xenkernar.pdlrms.utils.Result;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public Result handleAllUncaughtException(CustomException ex) {
        log.info("捕获到异常:"+ex.getMessage());
        return Result.error().code(ex.getErrCode()).message(ex.getMessage());
    }
}