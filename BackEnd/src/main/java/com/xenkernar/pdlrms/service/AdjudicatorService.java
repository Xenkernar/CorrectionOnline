package com.xenkernar.pdlrms.service;

import com.xenkernar.pdlrms.utils.adjudicator.CodeRunner;
import com.xenkernar.pdlrms.utils.adjudicator.LabReportFactory;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xenkernar.pdlrms.properties.AdjudicatorProperties;
import com.xenkernar.pdlrms.entity.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


@Component
public class AdjudicatorService {
    @Autowired
    private AdjudicatorProperties adjudicatorProperties;
    @Autowired
    private CodeRunner codeRunner;
    @Autowired
    private LabReportFactory labReportFactory;
    @Autowired
    public AdjudicatorService(
            AdjudicatorProperties adjudicatorProperties,
            CodeRunner codeRunner
    ) {
        this.adjudicatorProperties = adjudicatorProperties;
        this.codeRunner = codeRunner;
    }

    public LabResult adjudicateReport(File file, String language, ReportTemplate template, Map<Integer, List<TestCase>> testCases) throws IOException, InterruptedException {
        LabReport report = labReportFactory.createLabReport(file, language, template );
        Map<Integer, QuestionResult> questionResults = new HashMap<>();
        for (Map.Entry<Integer, Map<String, String>> entry : report.getSrcCodes().entrySet()) {
            Integer questionID = entry.getKey();
            questionResults.put(questionID,adjudicateQuestion(report.getLanguage(), entry.getValue(), testCases.get(questionID)));
        }
        return new LabResult(
                report.getLanguage(),
                report.getLabID(),
                report.getSection(),
                report.getStudentID(),
                report.getName(),
                report.getReportName(),
                "",
                100,
                questionResults);
    }


    private QuestionResult adjudicateQuestion(String language, Map<String, String> srcCode, List<TestCase> testCases) throws IOException, InterruptedException {
        UUID uuid = UUID.randomUUID();
        String path = String.join("/",adjudicatorProperties.getCompile().getSrcPath(),uuid.toString().replace("-", ""));
        generateWaitingCompileFolder(path, srcCode);
        String detail = codeRunner.run(language, path, testCases);
        if (!deleteDirectory(new File(path))){
            LogManager.getLogger(AdjudicatorService.class).error("清除临时文件失败，请手动清除");
        }
        return new QuestionResult("通过".equals(detail), detail);
    }

    private static void generateWaitingCompileFolder(String path, Map<String, String> sourceCodes) throws IOException{
        new File(path).mkdirs();
        // 生成源代码文件
        for (Map.Entry<String, String> entry : sourceCodes.entrySet()) {
            String fileName = entry.getKey();
            String content = entry.getValue();
            Files.write(Paths.get(path+"/"+fileName), content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static boolean deleteDirectory(File dir) {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    deleteDirectory(child);
                } else {
                    child.delete();
                }
            }
        }
        return dir.delete();
    }
}
