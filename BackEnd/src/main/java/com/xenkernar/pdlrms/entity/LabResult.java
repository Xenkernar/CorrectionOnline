package com.xenkernar.pdlrms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "lab_result")
public class LabResult {
    private String language;
    @Indexed
    private Integer labId;
    @Indexed
    private String section;
    @Indexed
    private String studentId;
    @Indexed
    private String name;
    @Indexed
    private String reportName;
    private String submittedTime;
    private double score;
    private Map<Integer,QuestionResult> questionResults;
}
