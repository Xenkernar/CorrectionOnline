package com.xenkernar.pdlrms.controller;

import com.xenkernar.pdlrms.service.FileService;
import com.xenkernar.pdlrms.service.ReportTemplateService;
import com.xenkernar.pdlrms.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ReportTemplateController {
    @Autowired
    private ReportTemplateService reportTemplateService;
    @Autowired
    private FileService fileService;

    @Operation(summary = "管理员上传模板文件",security = {@SecurityRequirement(name = "Authorization")})
    @PostMapping("/admin/template")
    @Secured("ROLE_ADMIN")
    public Result publishReport(
            @RequestPart("file") MultipartFile file,
            @RequestPart("templateId") String id,
            HttpServletRequest request
    )   {
        fileService.saveToLocal(file,request);
        reportTemplateService.post(id,file.getOriginalFilename(),request);
        fileService.deleteFromLocal(file.getOriginalFilename(),request);
        return Result.ok();
    }

    @Operation(summary = "管理员删除模板文件",security = {@SecurityRequirement(name = "Authorization")})
    @DeleteMapping("/admin/template")
    @Secured("ROLE_ADMIN")
    public Result deleteReport(
            @RequestParam("templateId") String id
    )   {
        reportTemplateService.delete(id);
        return Result.ok();
    }

    @Operation(summary = "管理员获取模板文件列表",security = {@SecurityRequirement(name = "Authorization")})
    @GetMapping("/admin/template/all")
    @Secured("ROLE_ADMIN")
    public Result getReportList() {
        return Result.ok().data("templates", reportTemplateService.getAll());
    }
}
