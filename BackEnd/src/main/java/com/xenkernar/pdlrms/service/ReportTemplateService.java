package com.xenkernar.pdlrms.service;

import com.xenkernar.pdlrms.entity.ReportTemplate;
import com.xenkernar.pdlrms.repository.ReportTemplateRepository;
import com.xenkernar.pdlrms.utils.MinioUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class ReportTemplateService {
    @Autowired
    private ReportTemplateRepository reportTemplateRepository;
    public void post(String id, String fileName, HttpServletRequest request) {
        String path = request.getServletContext().getRealPath("/reports/");
        File report = new File(path,fileName);
        reportTemplateRepository.save(new ReportTemplate(id,report));
    }

    public void delete(String id) {
        reportTemplateRepository.delete(id);
    }

    public ReportTemplate get(String id) {
        return reportTemplateRepository.findById(id);
    }

    public List<String> getAll() {
        return reportTemplateRepository.findAll().stream().map(ReportTemplate::getId).toList();
    }
}
