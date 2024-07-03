package com.xenkernar.pdlrms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xenkernar.pdlrms.handler.CustomException;
import com.xenkernar.pdlrms.repository.SubmitRecordRepository;

import java.util.Map;
import java.util.Set;

@Service
public class SubmittedRecordService {
    @Autowired
    private SubmitRecordRepository repository;

    public void addReport(String studentId, String labName, String reportName) {
        if (!repository.containsLab(labName)){
            throw new CustomException(203,"提交了未发布过的报告");
        }
        repository.submitReport(studentId, labName, reportName);
    }

    public void addLab(String labName) {
        repository.publishLab(labName);
    }

    public void removeReport(String studentId, String labName, String reportName) {
        repository.removeReport(studentId, labName, reportName);
    }

    public void removeLab(String labName) {
        repository.removeLab(labName);
    }

    public void removeReports(String studentId) {
        repository.removeReports(studentId);
    }

    public Map<String,String> getUserReports(String studentId) {
        return repository.getUserReports(studentId);
    }


    public Map<String,Integer> getSubmittedCounts() {
        return repository.getSubmittedCounts();
    }

    public Set<String> getSubmittedReports(String labName) {
        return repository.getSubmittedReports(labName);
    }


}
