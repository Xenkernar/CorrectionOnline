package com.xenkernar.pdlrms.utils.adjudicator;

import com.xenkernar.pdlrms.entity.LabReport;
import com.xenkernar.pdlrms.entity.ReportTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import com.xenkernar.pdlrms.properties.AdjudicatorProperties;

import java.io.File;
import java.io.IOException;

@Component
@EnableConfigurationProperties(AdjudicatorProperties.class)
public class LabReportFactory {
    private final AdjudicatorProperties adjudicatorProperties;

    public LabReportFactory(AdjudicatorProperties adjudicatorProperties){
        this.adjudicatorProperties = adjudicatorProperties;
    }

    public LabReport createLabReport(File file, String language, ReportTemplate template) throws IOException {
        return new LabReport(file,language,template,adjudicatorProperties);
    }
}
