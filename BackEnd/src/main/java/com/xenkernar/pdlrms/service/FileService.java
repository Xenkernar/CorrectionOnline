package com.xenkernar.pdlrms.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.xenkernar.pdlrms.entity.*;
import com.xenkernar.pdlrms.handler.CustomException;
import com.xenkernar.pdlrms.repository.PublishedReportRepository;
import com.xenkernar.pdlrms.utils.MinioUtils;
import com.xenkernar.pdlrms.utils.TimeUtils;
import com.xenkernar.pdlrms.utils.TimeUtils.TimeUnit;
import com.xenkernar.pdlrms.service.AdjudicatorService;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class FileService {
    @Autowired
    private MinioUtils minioUtils;

    @Autowired
    private GradeService gradeService;
    @Autowired
    private PublishedReportRepository publishedReportRepository;
    @Autowired
    private SubmittedReportService submittedReportService;
    @Autowired
    private ReportTemplateService reportTemplateService;
    @Autowired
    private AdjudicatorService adjudicatorService;

    public String saveToRemote(String dir, MultipartFile file){
        Map<String, String> map = minioUtils.upload(dir,file);
        if (map.containsKey("error")) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(),map.get("error"));
        }
        return map.get("fileName");
    }

    public String getUrl(String dir, String fileName)   {
        Map<String,String> map =  minioUtils.getUrl(dir,fileName);
        if (map.containsKey("error")) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() ,map.get("error"));
        }
        return map.get("url");
    }

    public void deleteFromRemote(String dir, String fileName)   {
        Map<String,String> map =  minioUtils.delete(dir,fileName);
        if (map.containsKey("error")) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(),map.get("error"));
        }
    }

    public void saveToLocal(MultipartFile file, HttpServletRequest request)   {
        String path = request.getServletContext().getRealPath("/reports/");
        try {
            saveFile(file, path);
        } catch (IOException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() ,"文件保存失败");
        }
    }

    public LabResult correctReport(String labName,String reportName,HttpServletRequest request)  {
        String path = request.getServletContext().getRealPath("/reports/");
        File report = new File(path,reportName);
        SubmittedReport submittedReport = submittedReportService.getByFileName(reportName);
        PublishedReport lab = publishedReportRepository.findByFileName(labName).getFirst();
        LabResult res;
        try {
            res =  adjudicatorService.adjudicateReport(report,lab.getLanguage(), reportTemplateService.get(lab.getTemplateId()), lab.getTestCases());
            deleteFromLocal(reportName,request);
        } catch (IOException | InterruptedException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST.value() , "批改失败");
        }
        res.setSubmittedTime(submittedReport.getSubmitDateTime());
        res.setStudentId(submittedReport.getStudentId());
        res.setSection(submittedReport.getSection());
        double score = 100.0;
        for (QuestionResult result : res.getQuestionResults().values()) {
            score -= result.isCorrect()?0:(100.0 /res.getQuestionResults().size());
        }
        if (lab.getDecScore() > 0) {
            long l = TimeUtils.timeCycle(
                    TimeUtils.timeToMillisecond(lab.getStartDateTime()),
                    TimeUtils.timeToMillisecond(submittedReport.getSubmitDateTime()),
                    lab.getGap(),
                    TimeUnit.valueOf(lab.getUnit())
            );
            score -= l * lab.getDecScore();
        }
        score = Math.max(score,0);
        Grade grade = new Grade();
        grade.setFileName(reportName);
        grade.setStudentId(submittedReport.getStudentId());
        grade.setLanguage(lab.getLanguage());
        grade.setLabId(lab.getLabId());
        grade.setScore(score);
        grade.setSection(submittedReport.getSection());
        gradeService.postGrade(grade);
        res.setScore(score);
        return res;
    }

    public void deleteFromLocal(String fileName, HttpServletRequest request)   {
        String path = request.getServletContext().getRealPath("/reports/");
        File file = new File(path,fileName);
        if (file.exists()) {
            file.delete();
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST.value(),"文件不存在");
        }
    }

    private void saveFile(MultipartFile file, String path) throws IOException{
        //判断文件夹是否存在
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        //保存文件
        File saveFile = new File(folder, Objects.requireNonNull(file.getOriginalFilename()));
        file.transferTo(saveFile);
    }

}
