package com.yzchnb.gpacrawler.demo.ExceptionHandlers;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeoutException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class) //@ExceptionHandler 该注解声明异常处理方法
    @ResponseBody
    public String defaultErrorHandler(HttpServletRequest req, Exception e) {
        return e.getMessage();
    }

    @ExceptionHandler(value = TimeoutException.class)
    @ResponseBody
    public String timeoutHandler(){
        return "time out";
    }
}
