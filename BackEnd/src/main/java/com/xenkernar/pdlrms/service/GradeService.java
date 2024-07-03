package com.xenkernar.pdlrms.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xenkernar.pdlrms.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xenkernar.pdlrms.entity.Grade;
import com.xenkernar.pdlrms.handler.CustomException;
import com.xenkernar.pdlrms.mapper.GradeMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GradeService {
    @Autowired
    private GradeMapper gradeMapper;

    public void postGrade(Grade grade) {
        if (gradeMapper.selectById(grade.getFileName()) != null) {
            gradeMapper.updateById(grade);
            return;
        }gradeMapper.insert(grade);
    }

    //计算所有成绩的平均分
    public Map<Integer,Double> getAverage(String language, String section) {
        QueryWrapper<Grade> queryWrapper = new QueryWrapper<>();
        Optional.ofNullable(language).ifPresent(l -> queryWrapper.eq("language", l));
        Optional.ofNullable(section).ifPresent(s -> queryWrapper.eq("section", s));
        return new HashMap<>(gradeMapper.selectList(queryWrapper)
                .stream().collect(Collectors.groupingBy(Grade::getLabId)).entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().mapToDouble(Grade::getScore).average().orElse(0.0)))
        );
    }

}
