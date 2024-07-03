package com.xenkernar.pdlrms.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xenkernar.pdlrms.service.DataService;
import com.xenkernar.pdlrms.service.GradeService;
import com.xenkernar.pdlrms.service.LabResultService;
import com.xenkernar.pdlrms.utils.Result;

import javax.annotation.Nullable;

@RestController
public class DataController {
    @Autowired
    DataService dataServer;
    @Autowired
    GradeService gradeService;
    @Autowired
    LabResultService labResultService;

    @Operation(summary = "清除所有数据")
    @PostMapping("/admin/resetAll")
    @Secured("ROLE_ADMIN")
    public Result resetAll() {
        dataServer.resetAll();
        return Result.ok();
    }

    @Operation(summary = "平均分")
    @GetMapping("/admin/grade/average")
    @Secured("ROLE_ADMIN")
    public Result averageGrade(
            @RequestParam @Nullable String language,
            @RequestParam @Nullable  String section) {
        return Result.ok().data("average",  gradeService.getAverage(language, section));
    }

    @Operation(summary = "通过率")
    @GetMapping("/admin/labResult/passRate")
    @Secured("ROLE_ADMIN")
    public Result passRate(
            @RequestParam @Nullable String language,
            @RequestParam @Nullable String section) {
        return Result.ok().data("passRate",  labResultService.getPassRate(language, section));
    }

    @Operation(summary = "按时率")
    @GetMapping("/admin/labResult/onTimeRate")
    @Secured("ROLE_ADMIN")
    public Result onTimeRate(
            @RequestParam @Nullable String language,
            @RequestParam @Nullable String section) {
        return Result.ok().data("onTimeRate",  labResultService.getOnTimeRate(language, section));
    }
}
