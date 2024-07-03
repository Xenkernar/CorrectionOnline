package com.xenkernar.pdlrms.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xenkernar.pdlrms.service.SubmittedRecordService;
import com.xenkernar.pdlrms.utils.Result;

import java.security.Principal;
import java.util.HashSet;

@RestController
public class SubmittedRecordController {
    @Autowired
    private SubmittedRecordService submittedRecordService;

    @Operation(summary = "删除我的所有提交记录")
    @DeleteMapping("/submittedRecord/myAll")
    @Secured("ROLE_USER")
    public Result deleteSubmittedRecord(Principal principal) {
        submittedRecordService.removeReports(principal.getName());
        return Result.ok();
    }

    @Operation(summary = "删除一条提交记录")
    @DeleteMapping("/submittedRecord")
    @Secured("ROLE_USER")
    public Result deleteSubmittedRecord(
            @RequestParam String labName,
            @RequestParam String reportName,
            Principal principal
    ) {
        submittedRecordService.removeReport(
                principal.getName(),
                labName,
                reportName);
        return Result.ok();
    }

    @Operation(summary = "获取我的所有提交记录")
    @GetMapping("/submittedRecord/user")
    @Secured("ROLE_USER")
    public Result getUserRecords(Principal principal) {
        return Result.ok().data("records", submittedRecordService.getUserReports(principal.getName()));
    }


    @Operation(summary = "获取所有报告的提交数量")
    @GetMapping("/submittedRecord/reports")
    @Secured("ROLE_ADMIN")
    public Result getReportRecords() {
        return Result.ok().data("records", submittedRecordService.getSubmittedCounts());
    }

    @Operation(summary = "获取某个实验的所有提交记录")
    @GetMapping("/submittedRecord/lab")
    @Secured("ROLE_ADMIN")
    public Result getLabRecords(@RequestParam String labName) {
        return Result.ok().data("records", new HashSet<>(submittedRecordService.getSubmittedReports(labName)));
    }

}
