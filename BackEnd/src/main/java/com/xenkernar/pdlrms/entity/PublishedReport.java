package com.xenkernar.pdlrms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "published_report")
public class PublishedReport {
    @Indexed
    private String language;
    @Indexed
    private Integer labId;
    @Indexed
    private String section;
    @Indexed
    private String fileName;

    private String templateId;

    private Map<Integer, List<TestCase>> testCases;

    private String publishDateTime;

    private String startDateTime;
    private Integer gap;
    private String unit;
    private Integer decScore;
}
