package com.xenkernar.pdlrms.repository;

import com.mongodb.client.MongoDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import com.xenkernar.pdlrms.entity.PublishedReport;

import java.util.List;

@Repository
public class PublishedReportRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public PublishedReportRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<PublishedReport> findAll(){
        return mongoTemplate.findAll(PublishedReport.class);
    }

    public List<PublishedReport> findBySection(String section){
        Query query = new Query().addCriteria(Criteria.where("section").is(section));
        return mongoTemplate.find(query, PublishedReport.class);
    }

    public List<PublishedReport> findByFileName(String fileName){
        Query query = new Query().addCriteria(Criteria.where("fileName").is(fileName));
        return mongoTemplate.find(query, PublishedReport.class);
    }

    public PublishedReport insert(PublishedReport publishedReport){
        return mongoTemplate.insert(publishedReport);
    }

    public PublishedReport save(PublishedReport PublishedReport){
        return mongoTemplate.save(PublishedReport);
    }

    public void deleteByFileName(String fileName){
        Query query = new Query().addCriteria(Criteria.where("fileName").is(fileName));
        mongoTemplate.remove(query, PublishedReport.class);
    }
    public void drop() {
        MongoDatabase database = mongoTemplate.getDb();
        database.getCollection("published_report").drop();
    }
}
