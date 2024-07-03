package com.xenkernar.pdlrms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xenkernar.pdlrms.entity.SubmittedReport;
import com.xenkernar.pdlrms.repository.SubmittedReportRepository;

import java.util.List;

@Service
public class SubmittedReportService {
    @Autowired
    private SubmittedReportRepository submittedReportRepository;

    public SubmittedReport getByFileName(String fileName) {
        List<SubmittedReport> reports = submittedReportRepository.findByFileName(fileName);
        if (reports.isEmpty()) {
            return null;
        }
        return reports.getFirst();
    }

    public void post(SubmittedReport submittedReport) {
        submittedReportRepository.insert(submittedReport);
    }

    public void deleteByFileName(String fileName) {
        submittedReportRepository.deleteByFileName(fileName);
    }
}
