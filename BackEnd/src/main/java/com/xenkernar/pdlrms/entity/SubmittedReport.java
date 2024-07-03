package com.xenkernar.pdlrms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "submitted_report")
public class SubmittedReport {
    @Indexed
    private String language;
    @Indexed
    private Integer labId;
    @Indexed
    private String section;
    @Indexed
    private String studentId;

    private String labName;

    private String fileName;

    private String submitDateTime;
}
