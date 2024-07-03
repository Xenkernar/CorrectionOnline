package com.xenkernar.pdlrms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.xenkernar.pdlrms.entity.ExtUser;
import com.xenkernar.pdlrms.entity.InvitationCode;
import com.xenkernar.pdlrms.entity.SysUser;
import com.xenkernar.pdlrms.handler.CustomException;
import com.xenkernar.pdlrms.mapper.InvitationCodeMapper;
import com.xenkernar.pdlrms.mapper.UserMapper;
import com.xenkernar.pdlrms.repository.SectionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InvitationCodeMapper invitationCodeMapper;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    //添加用户
    public void postUser(ExtUser extUser)   {
        if (userMapper.selectById(extUser.getId()) != null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(),"用户已存在");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("code", extUser.getCode());
        List<InvitationCode> sectionInfos = invitationCodeMapper.selectByMap(map);
        if (sectionInfos.isEmpty()) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() ,"请输入正确的邀请码");
        }
        SysUser sysUser = new SysUser();
        sysUser.setId(extUser.getId());
        sysUser.setUsername(extUser.getUsername());
        sysUser.setPassword(passwordEncoder.encode(extUser.getPassword()));
        sysUser.setSection(sectionInfos.getFirst().getSection());
        sectionRepository.addStudent(sectionInfos.getFirst().getSection(), extUser.getId());
        userMapper.insert(sysUser);
    }

    //添加用户
    public void postUser(SysUser user)   {
        if (userMapper.selectById(user.getId()) != null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() ,"用户已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        sectionRepository.addStudent(user.getSection(), user.getId());
        userMapper.insert(user);
    }

    //删除用户
    public void deleteUser(String id) {
        userMapper.deleteById(id);
    }


    //更新用户
    public void putUser(SysUser sysUser)   {
        SysUser user  = userMapper.selectById(sysUser.getId());
        if (user == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() ,"用户不存在");
        }
        if (!user.getSection().equals(sysUser.getSection())) {
            sectionRepository.removeStudent(user.getSection(), sysUser.getId());
            sectionRepository.addStudent(sysUser.getSection(),sysUser.getId());
        }
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        userMapper.updateById(sysUser);
    }

    public void updateUsername(String id,String username){
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() ,"用户不存在");
        }
        user.setUsername(username);
        userMapper.updateById(user);
    }

    public void updatePassword(String id,String password){
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() ,"用户不存在");
        }
        user.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }

    //根据id获取用户
    public SysUser getUserById(String id) {
        return userMapper.selectById(id);
    }

    //根据班级获取用户
    public List<SysUser> getBySection(String section) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(section).ifPresent(s -> queryWrapper.eq("section", s));
        return userMapper.selectList(queryWrapper);
    }

    public Page<SysUser> getBySections(List<String> sections,int page,int size) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        if (sections != null && !sections.isEmpty()) {
            queryWrapper.in("section", sections);
        }
        queryWrapper.ne("id","admin");
        return userMapper.selectPage(new Page<>(page, size), queryWrapper);
    }

    //根据班级删除用户
    public void deleteBySection(String section) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("section", section);
        sectionRepository.removeSection(section);
        userMapper.delete(queryWrapper);
    }

}
