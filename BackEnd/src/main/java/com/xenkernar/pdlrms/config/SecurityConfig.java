package com.xenkernar.pdlrms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import com.xenkernar.pdlrms.service.SecurityUserDetailsService;
import com.xenkernar.pdlrms.utils.jwt.*;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {
    @Autowired
    private SecurityUserDetailsService userDetailsService;

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint; //未登录处理类

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private JwtLoginSuccessHandler jwtLoginSuccessHandler; //登录成功处理类

    @Autowired
    private JwtLoginFailureHandler jwtLoginFailureHandler; //登录失败处理类

    @Autowired
    private MyAccessDeniedHandler myAccessDeniedHandler; //权限不足处理类

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter = new JwtAuthenticationTokenFilter();

        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests((authorize) -> authorize //开启登录配置
                .requestMatchers("/login", "/user","/logout").permitAll() //登录和登出接口不需要认证
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/swagger-ui.html",
                        "/favicon.ico",
                        "/webjars/**"
                ).permitAll()
                .anyRequest().authenticated() // 除了以上的请求，其它请求都需要认证
        );

        http.formLogin((login) -> login //开启自动配置的登录功能
                .loginProcessingUrl("/login") //自定义登录访问路径
                .usernameParameter("id") //自定义登录用户名参数
                .passwordParameter("password") //自定义登录密码参数
                .successHandler(jwtLoginSuccessHandler) //自定义登录成功的逻辑
                .failureHandler(jwtLoginFailureHandler) //自定义登录失败的逻辑
                .permitAll() //登录页面所有人可以访问
        );

        http.logout(LogoutConfigurer::permitAll);//开启自动配置的注销功能

        http.exceptionHandling((exceptionHandling) -> exceptionHandling //异常处理
                .authenticationEntryPoint(restAuthenticationEntryPoint) //未登录处理
                .accessDeniedHandler(myAccessDeniedHandler) //权限不足处理
        );

        http.headers((headers) -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));//允许同源iframe

        http.cors((corsConfiguration) -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
            config.setAllowedHeaders(Arrays.asList("*"));
            config.setAllowCredentials(true);
            corsConfiguration.configurationSource(request -> config);
        }); //开启跨域

        http.csrf(AbstractHttpConfigurer::disable); //关闭csrf防护

        http.userDetailsService(userDetailsService); //设置自定义的UserDetailsService

        http.sessionManagement((sessionManagement) -> sessionManagement //使用jwt,禁用session,不产生cookie
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
        return http.build();
    }
}