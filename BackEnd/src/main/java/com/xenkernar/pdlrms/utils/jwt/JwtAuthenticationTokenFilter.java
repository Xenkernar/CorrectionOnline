package com.xenkernar.pdlrms.utils.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //获取token
        String token = request.getHeader("Authorization");
        if (token != null) {
            Claims claims = JwtUtils.parseToken(token);
            //验证token
            //获取过期时间
            Date date = claims.getExpiration();
            //判断是否过期
            if (date.before(new Date())) {
                //过期
                response.setStatus(401);
                response.getWriter().write("token已过期");
                return;
            }
            //获取用户信息
            LinkedHashMap authenticationMap = (LinkedHashMap<String, Object>) claims.get("authentication");

            String sysUserId = (String)authenticationMap.get("name");
            ArrayList<LinkedHashMap<String, String>> authenticationList = (ArrayList<LinkedHashMap<String, String>>)authenticationMap.get("authorities");
            String[] authenticationStr = new String[authenticationList.size()];
            for (int i = 0; i < authenticationList.size(); i++) {
                authenticationStr[i] = authenticationList.get(i).get("authority");
            }
            List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(authenticationStr);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(sysUserId, null, authorities);
            //将用户信息放入SecurityContext中
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
