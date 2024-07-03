package com.xenkernar.pdlrms.repository;

import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import com.xenkernar.pdlrms.entity.LabResult;
import com.xenkernar.pdlrms.entity.PublishedReport;
import com.xenkernar.pdlrms.entity.QuestionResult;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class LabResultRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public LabResultRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Page<LabResult> findByOptionalCriteria(List<String> sections, List<String> languages, List<Integer> labIds, Pageable pageable) {
        Query query = new Query().with(pageable);
        if (!sections.isEmpty()) {
            query.addCriteria(Criteria.where("section").in(sections));
        }
        if (!languages.isEmpty()) {
            query.addCriteria(Criteria.where("language").in(languages));
        }
        if (!labIds.isEmpty()) {
            query.addCriteria(Criteria.where("labId").in(labIds));
        }
        List<LabResult> labResults = mongoTemplate.find(query, LabResult.class);
        long total = mongoTemplate.count(query.limit(-1).skip(-1), LabResult.class);
        return new PageImpl<>(labResults, pageable, total);
    }

    public List<LabResult> findByStudentId(String studentId) {
        Query query = new Query().addCriteria(Criteria.where("studentId").is(studentId));
        return mongoTemplate.find(query, LabResult.class);
    }

    public Page<LabResult> findByStudentId(String studentId,Pageable pageable) {
        Query query = new Query().with(pageable).addCriteria(Criteria.where("studentId").is(studentId));
        List<LabResult> labResults = mongoTemplate.find(query, LabResult.class);
        long total = mongoTemplate.count(query.limit(-1).skip(-1), LabResult.class);
        return new PageImpl<>(labResults, pageable, total);
    }


    public void insert(LabResult labResult) {
        mongoTemplate.insert(labResult);
    }

    public void deleteByReportName(String reportName) {
        Query query = new Query().addCriteria(Criteria.where("reportName").is(reportName));
        mongoTemplate.remove(query, LabResult.class);
    }

    //通过率
    public Map<Integer,Double> getPassRate(String language, String section) {
        Map<Integer,Double> res = new HashMap<>();
        Query query = new Query();
        Optional.ofNullable(language).ifPresent(l -> query.addCriteria(Criteria.where("language").is(l)));
        Optional.ofNullable(section).ifPresent(s -> query.addCriteria(Criteria.where("section").is(s)));
        List<LabResult> labResults = mongoTemplate.find(query, LabResult.class);
        Map<Integer, List<LabResult>> idResmap = new HashMap<>(labResults.stream().collect(Collectors.groupingBy(LabResult::getLabId)));
        idResmap.keySet().forEach(id -> {
            List<LabResult> results = idResmap.get(id);
            int passCount = 0;
            for (LabResult labResult : results) {
                boolean pass = true;
                for (QuestionResult questionResult : labResult.getQuestionResults().values()) {
                    if (!questionResult.isCorrect()) {
                        pass = false;
                        break;
                    }
                }
                passCount += pass ? 1 : 0;
            }
            res.put(id, (double) passCount / results.size());
        });
        return res;
    }
    //准时率
    public Map<Integer,Double> getOnTimeRate(String language,String section) {
        Map<Integer,Double> res = new HashMap<>();
        Query query = new Query();
        Optional.ofNullable(language).ifPresent(l -> query.addCriteria(Criteria.where("language").is(l)));
        Optional.ofNullable(section).ifPresent(s -> query.addCriteria(Criteria.where("section").is(s)));
        List<PublishedReport> publishedReports = mongoTemplate.find(query, PublishedReport.class);
        HashMap<String, PublishedReport> reportMap = new HashMap<>(
                publishedReports.stream().collect(Collectors.toMap(report -> report.getLanguage()+report.getSection()+report.getLabId(), report -> report)));
        List<LabResult> labResults = mongoTemplate.find(query, LabResult.class);
        Map<Integer, List<LabResult>> idResmap = new HashMap<>(labResults.stream().collect(Collectors.groupingBy(LabResult::getLabId)));
        idResmap.keySet().forEach(id -> {
            List<LabResult> results = idResmap.get(id);
            int ontimeCount = 0;
            for (LabResult labResult : results) {
                PublishedReport publishedReport = reportMap.get(labResult.getLanguage() + labResult.getSection() + labResult.getLabId());
                if (publishedReport == null){continue;}
                if (publishedReport.getDecScore() == 0 ||labResult.getSubmittedTime().compareTo(publishedReport.getStartDateTime()) <= 0) {
                    ontimeCount++;
                }
            }
            res.put(id, (double) ontimeCount / results.size());
        });
        return res;
    }

    public void drop() {
        MongoDatabase database = mongoTemplate.getDb();
        database.getCollection("lab_result").drop();
    }
}
