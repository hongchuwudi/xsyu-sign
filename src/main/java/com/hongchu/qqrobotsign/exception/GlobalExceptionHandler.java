package com.hongchu.qqrobotsign.exception;

import com.hongchu.qqrobotsign.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<String> exceptionHandler(BusinessException ex){
        log.error("业务异常信息：{}", ex.getMessage());
        return Result.fail(ex.getMessage());
    }

    /**
     * 捕获SQL唯一约束异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        log.error("SQL异常信息：{}", message);

        if(message.contains("Duplicate entry")){
            String[] split = message.split(" ");
            String username = split[2];
            String msg = username + "早已存在";
            return Result.fail(msg);
        }else{
            return Result.fail("数据库操作失败");
        }
    }

    /**
     * 捕获所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception ex){
        log.error("系统异常信息：", ex);
        return Result.fail("系统繁忙，请稍后重试");
    }
}