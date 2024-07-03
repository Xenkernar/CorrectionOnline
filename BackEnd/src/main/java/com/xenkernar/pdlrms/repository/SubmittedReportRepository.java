package com.xenkernar.pdlrms.repository;

import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import com.xenkernar.pdlrms.entity.SubmittedReport;

import java.util.List;

@Repository
public class SubmittedReportRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public SubmittedReportRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<SubmittedReport> findAll(){
        return mongoTemplate.findAll(SubmittedReport.class);
    }

    public List<SubmittedReport> findByFileName(String fileName){
        Query query = new Query().addCriteria(Criteria.where("fileName").is(fileName));
        return mongoTemplate.find(query, SubmittedReport.class);
    }

    public SubmittedReport insert(SubmittedReport submittedReport){
        return mongoTemplate.insert(submittedReport);
    }

    //更新
    public SubmittedReport save(SubmittedReport submittedReport){
        return mongoTemplate.save(submittedReport);
    }

    public void deleteByFileName(String fileName){
        Query query = new Query().addCriteria(Criteria.where("fileName").is(fileName));
        mongoTemplate.remove(query, SubmittedReport.class);
    }

    public void drop() {
        MongoDatabase database = mongoTemplate.getDb();
        database.getCollection("submitted_report").drop();
    }
}

