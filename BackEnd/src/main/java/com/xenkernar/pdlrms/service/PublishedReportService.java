package com.xenkernar.pdlrms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xenkernar.pdlrms.entity.PublishedReport;
import com.xenkernar.pdlrms.repository.PublishedReportRepository;

import java.util.List;

@Service
public class PublishedReportService {
    @Autowired
    private PublishedReportRepository  publishedReportRepository;

    //查找
    public List<PublishedReport> findAll() {
        return publishedReportRepository.findAll();
    }

    public List<PublishedReport> getBySection(String section) {
        return publishedReportRepository.findBySection(section);
    }

    //插入
    public void post(PublishedReport publishedReport) {
        publishedReportRepository.insert(publishedReport);
    }


    public void deleteByFileName(String fileName) {
        publishedReportRepository.deleteByFileName(fileName);
    }

}
