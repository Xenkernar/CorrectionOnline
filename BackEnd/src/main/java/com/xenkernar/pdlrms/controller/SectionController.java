package com.xenkernar.pdlrms.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import com.xenkernar.pdlrms.entity.ExtSection;
import com.xenkernar.pdlrms.service.SectionService;
import com.xenkernar.pdlrms.utils.Result;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class SectionController {
    @Autowired
    private SectionService sectionService;

    @Operation(summary = "获取班级列表")
    @GetMapping("/section/all")
    @Secured("ROLE_ADMIN")
    public Result getSections() {
        //映射到ExtSection
        List<ExtSection> sections = sectionService.getSections().stream()
                .map(section -> new ExtSection(section.getSection(), section.getCode()))
                .toList();
        return Result.ok().data("sections", sections);
    }

    @Operation(summary = "添加班级")
    @PostMapping("/section")
    @Secured("ROLE_ADMIN")
    public Result postSection(@RequestBody ExtSection section)   {
        sectionService.postSection(section);
        return Result.ok();
    }

    @Operation(summary = "删除班级")
    @DeleteMapping("/section")
    @Secured("ROLE_ADMIN")
    public Result deleteSection(@RequestParam String section) {
        sectionService.deleteSection(section);
        return Result.ok();
    }

    @Operation(summary = "更新班级")
    @PutMapping("/section")
    @Secured("ROLE_ADMIN")
    public Result putSection(@RequestBody ExtSection section)   {
        sectionService.putSection(section);
        return Result.ok();
    }

    @Operation(summary = "获取各班级人数")
    @GetMapping("/section/counts")
    @Secured("ROLE_ADMIN")
    public Result getSectionCounts() {
        Map<String, Integer> sections = sectionService.getCounts();
        return Result.ok().data("counts", sections);
    }

    @Operation(summary = "获取班级成员")
    @GetMapping("/section/members")
    @Secured("ROLE_ADMIN")
    public Result getSectionMembers(@RequestParam String section) {
        Set<String> members = sectionService.getMembers(section);
        return Result.ok().data("members", members);
    }
}
