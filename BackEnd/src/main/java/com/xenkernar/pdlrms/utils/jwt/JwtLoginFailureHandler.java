package com.xenkernar.pdlrms.utils.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import com.xenkernar.pdlrms.utils.Result;

import java.io.IOException;

@Component("jwtLoginFailureHandler")
public class JwtLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        String errorMsg = "";
        HttpStatus status;
        if (exception instanceof UsernameNotFoundException) {
            errorMsg = "用户名不存在";
            status = HttpStatus.NOT_FOUND;
        } else if (exception instanceof LockedException){
            errorMsg = "用户被锁定";
            status = HttpStatus.LOCKED;
        } else if (exception instanceof BadCredentialsException){
            errorMsg = "用户名或密码错误";
            status = HttpStatus.UNAUTHORIZED;
        }else {
            errorMsg = exception.getMessage();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        response.getWriter().write(objectMapper.writeValueAsString(Result.error().message(errorMsg).code(status.value())));
    }
}
