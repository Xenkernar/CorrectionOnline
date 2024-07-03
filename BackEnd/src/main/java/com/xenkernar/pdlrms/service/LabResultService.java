package com.xenkernar.pdlrms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.xenkernar.pdlrms.entity.LabResult;
import com.xenkernar.pdlrms.repository.LabResultRepository;

import java.util.List;
import java.util.Map;

@Service
public class LabResultService {
    @Autowired
    private LabResultRepository labResultRepository;

    //添加实验结果
    public void post(LabResult labResult) {
        labResultRepository.insert(labResult);
    }

    //删除实验结果
    public void delete(String reportName) {
        labResultRepository.deleteByReportName(reportName);
    }


    //根据学生学号获取实验结果
    public Page<LabResult> getByStudentId(String studentId, int page, int size) {
        return labResultRepository.findByStudentId(studentId, PageRequest.of(page, size, Sort.by(Sort.Order.asc("labId"))));
    }

    public List<LabResult> getByStudentId(String studentId) {
        return labResultRepository.findByStudentId(studentId);
    }


    //根据多个字段分页查询
    public Page<LabResult> getByMultipleFields(
            List<String> sections,
            List<String> languages,
            List<Integer> labIds,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("studentId")));
        return labResultRepository.findByOptionalCriteria(sections, languages, labIds, pageable);
    }

    public Map<Integer, Double> getPassRate(String language, String section) {
        return labResultRepository.getPassRate(language,section);
    }

    public Map<Integer,Double> getOnTimeRate(String language,String section) {
        return labResultRepository.getOnTimeRate(language,section);
    }

}
