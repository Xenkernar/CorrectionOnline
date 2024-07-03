package com.xenkernar.pdlrms.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Repository
public class SectionRepository {
    @Autowired
    StringSetRepository stringSetRepository;

    private static final String SECTION_COUNTS_PREFIX = "section:";

    public void addSection(String section) {
        stringSetRepository.addValue("sections", section);
    }

    public void removeSection(String section) {
        stringSetRepository.removeValue("sections", section);
        stringSetRepository.removeValues(SECTION_COUNTS_PREFIX + section);
    }

    public void addStudent(String section,String studentId) {
        stringSetRepository.addValue(SECTION_COUNTS_PREFIX+section, studentId);
    }

    public void removeStudent(String section,String studentId) {
        stringSetRepository.removeValue(SECTION_COUNTS_PREFIX+section, studentId);
    }

    public Integer getSectionCount(String section) {
        return stringSetRepository.getValues(SECTION_COUNTS_PREFIX+section).size();
    }

    public Set<String> getMembers(String section) {
        return stringSetRepository.getValues(SECTION_COUNTS_PREFIX+section);
    }

    public Map<String,Integer> getSectionCounts() {
        Set<String> values = stringSetRepository.getValues("sections");
        Map<String,Integer> res = new HashMap<>();
        for (String section : values) {
            res.put(section, getSectionCount(section));
        }
        return res;
    }


}
