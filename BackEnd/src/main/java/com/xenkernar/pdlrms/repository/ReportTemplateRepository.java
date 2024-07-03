package com.xenkernar.pdlrms.repository;

import com.mongodb.client.MongoDatabase;
import com.xenkernar.pdlrms.entity.ReportTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReportTemplateRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public ReportTemplateRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void save(ReportTemplate reportTemplate) {
        mongoTemplate.save(reportTemplate);
    }

    public void delete(String id) {
        Query query = new Query().addCriteria(Criteria.where("id").is(id));
        mongoTemplate.remove(query, ReportTemplate.class);
    }

    public ReportTemplate findById(String id) {
        Query query = new Query().addCriteria(Criteria.where("id").is(id));
        return mongoTemplate.findOne(query, ReportTemplate.class);
    }
    public List<ReportTemplate> findAll() {
        return mongoTemplate.findAll(ReportTemplate.class);
    }
    public void drop() {
        MongoDatabase database = mongoTemplate.getDb();
        database.getCollection("report_template").drop();
    }
}
