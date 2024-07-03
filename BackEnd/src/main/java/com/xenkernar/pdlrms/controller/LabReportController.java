package com.xenkernar.pdlrms.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.xenkernar.pdlrms.properties.AdjudicatorProperties;
import com.xenkernar.pdlrms.entity.LabResult;
import com.xenkernar.pdlrms.entity.PublishedReport;
import com.xenkernar.pdlrms.entity.SubmittedReport;
import com.xenkernar.pdlrms.handler.CustomException;
import com.xenkernar.pdlrms.service.*;
import com.xenkernar.pdlrms.utils.Result;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class LabReportController {
    @Autowired
    FileService fileService;
    @Autowired
    PublishedReportService publishedReportService;
    @Autowired
    SubmittedReportService submittedReportService;
    @Autowired
    SubmittedRecordService submittedRecordService;
    @Autowired
    LabResultService labResultService;
    @Autowired
    AdjudicatorProperties adjudicatorProperties;


    @Operation(summary = "管理员上传实验报告及测试用例",security = {@SecurityRequirement(name = "Authorization")})
    @PostMapping("/admin/labReport")
    @Secured("ROLE_ADMIN")
    public Result publishReport(
            @RequestPart("file") MultipartFile file,
            @RequestPart("report") PublishedReport report,
            @RequestPart("sections") String sections
    )   {
        String minioName = fileService.saveToRemote("ADMIN",file);
        submittedRecordService.addLab(report.getFileName());
        for (String section : sections.split(",")) {
            report.setSection(section);
            publishedReportService.post(report);
        }
        return Result.ok().data("fileName", minioName);
    }

    @Operation(summary = "管理员获取发布过的报告")
    @GetMapping("/publishedReports")
    @Secured("ROLE_ADMIN")
    public Result getPublishedReports(){
        return Result.ok().data("reports",publishedReportService.findAll());
    }

    @Operation(summary = "管理员删除发布的报告")
    @DeleteMapping("/admin/publishedReports")
    @Secured("ROLE_ADMIN")
    public Result deletePublishedReports(@RequestParam("fileName") String fileName)   {
        publishedReportService.deleteByFileName(fileName);
        fileService.deleteFromRemote("ADMIN",fileName);
        submittedRecordService.removeLab(fileName);
        return Result.ok();
    }

    @Operation(summary = "获取下载url")
    @GetMapping("/admin/labReport/urls")
    @Secured("ROLE_ADMIN")
    public Result adminDownloadReport(@RequestParam List<String> fileNames)   {
        ArrayList<String> urls = new ArrayList<>();
        for (String fileName : fileNames) {
            urls.add(fileService.getUrl("USER",fileName));
        }
        return Result.ok().data("urls", urls);
    }

    @Operation(summary = "提交实验报告",security = {@SecurityRequirement(name = "Authorization")})
    @PostMapping(value = "/user/labReport")
    @Secured("ROLE_USER")
    public Result userUploadReport(
            @RequestPart("file") MultipartFile file,
            @RequestPart("report") SubmittedReport report,
            Principal principal,
            HttpServletRequest request
    )   {
        if (!principal.getName().equals(report.getStudentId())) {
            throw new CustomException(HttpStatus.FORBIDDEN.value(),"无权限");
        }
        submittedRecordService.addReport(principal.getName(),report.getLabName(),report.getFileName());
        String minioName = fileService.saveToRemote("USER",file);
        fileService.saveToLocal(file,request);
        submittedReportService.deleteByFileName(report.getFileName());
        submittedReportService.post(report);
        return Result.ok().data("fileName", minioName);
    }

    @Operation(summary = "管理员代理提交",security = {@SecurityRequirement(name = "Authorization")})
    @PostMapping(value = "/admin/submit/proxy")
    @Secured("ROLE_ADMIN")
    public Result adminProxySubmit(
            @RequestPart("file") MultipartFile file,
            @RequestPart("report") SubmittedReport report,
            HttpServletRequest request
    )   {
        submittedRecordService.addReport(report.getStudentId(),report.getLabName(),report.getFileName());
        fileService.saveToRemote("USER",file);
        fileService.saveToLocal(file,request);
        submittedReportService.deleteByFileName(report.getFileName());
        submittedReportService.post(report);
        LabResult labResult = fileService.correctReport(report.getLabName(), report.getFileName(), request);
        labResultService.delete(report.getFileName());
        labResultService.post(labResult);
        return Result.ok().data("result", labResult);
    }

    @Operation(summary = "用户获取自己班级发布的报告")
    @GetMapping("/publishedReports/section")
    @Secured("ROLE_USER")
    public Result getUserSectionReports(@RequestParam String section){
        return Result.ok().data("reports",publishedReportService.getBySection(section));
    }


    @Operation(summary = "下载发布的实验报告",security = {@SecurityRequirement(name = "Authorization")})
    @GetMapping("/publishedReports/url")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
    public Result userDownloadReport(@RequestParam("fileName") String fileName)   {
        return Result.ok().data("url", fileService.getUrl("ADMIN",fileName));
    }

    @Operation(summary = "批改实验报告",security = {@SecurityRequirement(name = "Authorization")})
    @PostMapping("/user/labReport/correction")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
    public Result correctReport(
            @RequestParam("labName") String labName,
            @RequestParam("reportName") String reportName,
            HttpServletRequest request
    )   {
        LabResult labResult = fileService.correctReport(labName, reportName, request);
        labResultService.delete(reportName);
        labResultService.post(labResult);
        return Result.ok().data("result", labResult);
    }


    @Operation(summary = "获取支持的语言")
    @GetMapping("/labReport/languages")
    @Secured("ROLE_ADMIN")
    public Result getSupportedLanguages() {
        return Result.ok().data("languages", adjudicatorProperties.getLanguages().getSupport());
    }


}
