package com.xenkernar.pdlrms.entity;

import lombok.Data;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.stereotype.Component;
import com.xenkernar.pdlrms.properties.AdjudicatorProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@Data
@Component
public class LabReport {
    private Set<String> postfixSet;

    private Integer labID;
    private String Section;
    private String studentID;
    private String name;
    private String language;
    private String reportName;
    private Map<Integer,Map<String,String>> srcCodes;

    private LabReport() {
    }

    public LabReport(File file, String language, ReportTemplate reportTemplate, AdjudicatorProperties adjudicatorProperties) throws IOException {
        this.language = language;
        postfixSet = adjudicatorProperties.getLanguages().getSrcFileSuffix().get(language);
        FileInputStream fis = new FileInputStream(file.getAbsolutePath());
        XWPFDocument document = new XWPFDocument(fis);
        List<XWPFTable> tables = document.getTables();

        labID = Integer.parseInt(document.getParagraphArray(0).getText().split(" ")[reportTemplate.getLabIdIndex()]);

        Coordinate sectionCoordinate = reportTemplate.getSectionCoordinate();
        Coordinate studentNameCoordinate = reportTemplate.getStudentNameCoordinate();
        Coordinate studentIdCoordinate = reportTemplate.getStudentIdCoordinate();
        Section = tables
                .get(sectionCoordinate.getTableIndex())
                .getRows().get(sectionCoordinate.getRowIndex())
                .getTableCells().get(sectionCoordinate.getColumnIndex())
                .getText();
        studentID = tables
                .get(studentIdCoordinate.getTableIndex())
                .getRows().get(studentIdCoordinate.getRowIndex())
                .getTableCells().get(studentIdCoordinate.getColumnIndex())
                .getText();
        name = tables
                .get(studentNameCoordinate.getTableIndex())
                .getRows().get(studentNameCoordinate.getRowIndex())
                .getTableCells().get(studentNameCoordinate.getColumnIndex())
                .getText();

        reportName = file.getName();

        Map<Integer, Coordinate> codesCoordinates = reportTemplate.getCodesCoordinates();
        //题目与代码的映射
        srcCodes = new HashMap<>();
        codesCoordinates.keySet().forEach(i -> {
            Coordinate coordinate = codesCoordinates.get(i);
            Map<String,String> filenameCodeMap = new HashMap<>();
            StringBuilder stringBuilder = new StringBuilder();
            AtomicReference<String> filename = new AtomicReference<>("");
            tables.get(coordinate.getTableIndex())
                    .getRows().get(coordinate.getRowIndex())
                    .getTableCells().get(coordinate.getColumnIndex())
                    .getParagraphs().forEach(p -> {
                String text = p.getText();
                if(text.startsWith("(")){
                    return;
                }
                if (isFilename(text)) {
                    if (!filename.get().isEmpty()) {
                        filenameCodeMap.put(filename.get(), stringBuilder.toString());
                    }
                    filename.set(text);
                    stringBuilder.setLength(0);
                }else{
                    stringBuilder.append(text);
                    stringBuilder.append("\n");
                }
            });
            filenameCodeMap.put(filename.get(), stringBuilder.toString());
            srcCodes.put(i, filenameCodeMap);
        });

        fis.close();

    }

    private boolean isFilename(String filename) {
        //只由字母、数字、下划线，且不以数字开头，且必须以：:！结尾
        String[] nameAndPostfix = filename.trim().split("\\.");
        return nameAndPostfix[0].matches("^[a-zA-Z_][a-zA-Z0-9_]*$")&&postfixSet.contains(nameAndPostfix[1]);

    }



}
