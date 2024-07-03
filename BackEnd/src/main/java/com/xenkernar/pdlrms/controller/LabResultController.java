package com.xenkernar.pdlrms.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.xenkernar.pdlrms.entity.LabResult;
import com.xenkernar.pdlrms.service.LabResultService;
import com.xenkernar.pdlrms.utils.Result;

import java.security.Principal;
import java.util.List;

@RestController
@Slf4j
public class LabResultController {
    @Autowired
    private LabResultService labResultService;


    @Operation(summary = "多字段查询")
    @GetMapping("/labResults/multiFields")
    @Secured("ROLE_ADMIN")
    public Result getLabResultsByPage(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "sections",required = false) List<String> sections,
            @RequestParam(value = "Dockerfiles/languages",required = false) List<String> languages,
            @RequestParam(value = "labIds",required = false) List<Integer> labIds
    ) {
        sections = sections == null ? List.of() : sections;
        languages = languages == null ? List.of() : languages;
        labIds = labIds == null ? List.of() : labIds;
        Page<LabResult> byMultipleFields = labResultService.getByMultipleFields(sections, languages, labIds, page, size);
        return Result.ok().data("results",byMultipleFields.getContent()).data("total",byMultipleFields.getTotalElements());
    }

    @Operation(summary = "根据学号查询")
    @GetMapping("/labResults/studentId")
    @Secured("ROLE_ADMIN")
    public Page<LabResult> getLabResultById(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("studentId") String id
    ) {
        return labResultService.getByStudentId(id, page, size);
    }

    @Operation(summary = "学生获取实验结果")
    @GetMapping("/user/labResult/all")
    @Secured("ROLE_USER")
    public Result getLabResult(
            Principal principal
    )   {
        List<LabResult> results = labResultService.getByStudentId(principal.getName());
        return Result.ok().data("results", results);
    }

}
