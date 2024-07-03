package com.xenkernar.pdlrms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.xenkernar.pdlrms.entity.SysUser;
import com.xenkernar.pdlrms.mapper.UserMapper;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class SecurityUserDetailsService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getUserRole()));
        return new User(user.getId(),
                user.getPassword(),
                authorities);
    }
}
