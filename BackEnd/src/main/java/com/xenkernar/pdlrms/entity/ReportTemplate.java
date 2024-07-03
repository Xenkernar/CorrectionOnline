package com.xenkernar.pdlrms.entity;


import ch.qos.logback.core.joran.sanity.Pair;
import com.xenkernar.pdlrms.handler.CustomException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "report_template")
public class ReportTemplate {
    @Indexed
    private String id;
    private Integer labIdIndex;
    private Coordinate sectionCoordinate;
    private Coordinate studentIdCoordinate;
    private Coordinate studentNameCoordinate;
    private Map<Integer, Coordinate> codesCoordinates;

    @SneakyThrows
    public ReportTemplate(String id, File file){
        this.id = id;
        codesCoordinates = new HashMap<>();
        FileInputStream fis = new FileInputStream(file.getAbsolutePath());
        XWPFDocument document = new XWPFDocument(fis);
        List<XWPFTable> tables = document.getTables();
        String[] array = document.getParagraphArray(0).getText().split(" ");
        labIdIndex = IntStream.range(0, array.length).filter(i -> "<编号>".equals(array[i])).findFirst().orElse(-1);
        int count = 1;
        for (int i = 0; i < tables.size(); i++) {
            XWPFTable table = tables.get(i);
            for (int j = 0; j < table.getNumberOfRows(); j++) {
                XWPFTableRow row = table.getRow(j);
                for (int k = 0; k < row.getTableCells().size(); k++) {
                    XWPFTableCell cell = row.getTableCells().get(k);
                    String text = cell.getText().trim();
                    if ("<班级>".equals(text)) {
                        sectionCoordinate = new Coordinate(i, j, k);
                    }
                    if ("<学号>".equals(text)) {
                        studentIdCoordinate = new Coordinate(i, j, k);
                    }
                    if ("<姓名>".equals(text)) {
                        studentNameCoordinate = new Coordinate(i, j, k);
                    }
                    if ("<代码>".equals(text)) {
                        codesCoordinates.put(count++, new Coordinate(i, j, k));
                    }
                }
            }
        }
        if (sectionCoordinate == null
                || studentIdCoordinate == null
                || studentNameCoordinate == null
                || codesCoordinates.isEmpty()
                || labIdIndex == -1
        ) {
            throw new CustomException(235,"模板中缺少必要字段");
        }
    }
}
