package com.xenkernar.pdlrms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.xenkernar.pdlrms.entity.ExtSection;
import com.xenkernar.pdlrms.entity.InvitationCode;
import com.xenkernar.pdlrms.entity.SysUser;
import com.xenkernar.pdlrms.handler.CustomException;
import com.xenkernar.pdlrms.mapper.InvitationCodeMapper;
import com.xenkernar.pdlrms.repository.SectionRepository;
import com.xenkernar.pdlrms.repository.SubmitRecordRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SectionService {
    @Autowired
    private InvitationCodeMapper invitationCodeMapper;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SubmitRecordRepository submitRecordRepository;

    public void postSection(ExtSection section)   {
        if (invitationCodeMapper.selectById(section.getSection()) != null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(),"班级已存在");
        }
        String byCode = getSectionByCode(section.getCode());
        if (byCode != null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(),
                    "该邀请码已被班级\"" + byCode + "\"使用"
            );
        }
        InvitationCode invitationCode = new InvitationCode();
        invitationCode.setSection(section.getSection());
        invitationCode.setCode(section.getCode());
        invitationCodeMapper.insert(invitationCode);
        sectionRepository.addSection(section.getSection());
    }

    public void deleteSection(String section) {
        if (invitationCodeMapper.selectById(section) != null) {
            invitationCodeMapper.deleteById(section);
            List<SysUser> users = userService.getBySection(section);
            for (SysUser user : users) {
                submitRecordRepository.removeReports(user.getId());
            }
            userService.deleteBySection(section);
        }
        sectionRepository.removeSection(section);
    }

    public void putSection(ExtSection section)   {
        if (invitationCodeMapper.selectById(section.getSection()) == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() ,"班级不存在");
        }
        InvitationCode invitationCode = new InvitationCode();
        invitationCode.setSection(section.getSection());
        invitationCode.setCode(section.getCode());
        invitationCodeMapper.updateById(invitationCode);
    }

    public List<InvitationCode> getSections() {
        return invitationCodeMapper.selectList(null);
    }

    //根据邀请码查找班级
    public String getSectionByCode(String code) {
        QueryWrapper<InvitationCode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        InvitationCode invitationCode = invitationCodeMapper.selectOne(queryWrapper);
        return invitationCode == null ? null : invitationCode.getSection();
    }

    public Map<String,Integer> getCounts() {
        return sectionRepository.getSectionCounts();
    }

    public Set<String> getMembers(String section) {
        return sectionRepository.getMembers(section);
    }

}
