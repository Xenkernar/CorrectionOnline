package com.xenkernar.pdlrms.utils.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.xenkernar.pdlrms.entity.SysUser;
import com.xenkernar.pdlrms.mapper.UserMapper;
import com.xenkernar.pdlrms.utils.Result;

import java.io.IOException;

@Component("jwtLoginSuccessHandler")
public class JwtLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String token = JwtUtils.generateToken(authentication);
        response.setContentType("application/json;charset=utf-8");
        String role = ((UserDetails) authentication.getPrincipal()).getAuthorities().toArray()[0].toString();
        SysUser user = userMapper.selectById(((UserDetails) authentication.getPrincipal()).getUsername());
        response.getWriter().write(
            objectMapper.writeValueAsString(
                Result.ok()
                   .data("token", token)
                   .data("name", user.getUsername())
                   .data("role", role)
                   .data("section", user.getSection())
            )
        );
    }

}
