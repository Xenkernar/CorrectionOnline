package com.xenkernar.pdlrms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xenkernar.pdlrms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xenkernar.pdlrms.entity.SysUser;
import com.xenkernar.pdlrms.mapper.GradeMapper;
import com.xenkernar.pdlrms.mapper.InvitationCodeMapper;
import com.xenkernar.pdlrms.mapper.UserMapper;
import com.xenkernar.pdlrms.utils.MinioUtils;

@Service
public class DataService {
    @Autowired
    LabResultRepository labResultRepository;
    @Autowired
    PublishedReportRepository publishedReportRepository;
    @Autowired
    SubmittedReportRepository submittedReportRepository;
    @Autowired
    ReportTemplateRepository reportTemplateRepository;
    @Autowired
    StringSetRepository stringSetRepository;
    @Autowired
    MinioUtils minioUtils;
    @Autowired
    UserMapper userMapper;
    @Autowired
    InvitationCodeMapper invitationCodeMapper;
    @Autowired
    GradeMapper gradeMapper;


    public void resetAll() {
        labResultRepository.drop();
        publishedReportRepository.drop();
        submittedReportRepository.drop();
        reportTemplateRepository.drop();
        stringSetRepository.clearAll();
        minioUtils.clearAll();
        invitationCodeMapper.delete(null);
        gradeMapper.delete(null);
        userMapper.delete(new QueryWrapper<SysUser>().ne("id", "admin"));
    }
}
