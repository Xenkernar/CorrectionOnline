package com.xenkernar.pdlrms.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Repository
public class SubmitRecordRepository {

    @Autowired
    StringSetRepository stringSetRepository;

    /*
      publish lab1 do: [labs].add(lab1)
      student 123 submit myLab1 to lab1 do:
        [students:123].add(lab1:myLab1)
        [labs:lab1].add(123:myLab1)
     */

    private static final String STUDENTS_PREFIX = "students:";
    private static final String LABS_PREFIX = "labs:";

    public void publishLab(String lab) {
        stringSetRepository.addValue("labs", lab);
    }

    public void submitReport(String studentId, String labName, String reportName) {
        stringSetRepository.addValue(STUDENTS_PREFIX + studentId, labName + ":" + reportName);
        stringSetRepository.addValue(LABS_PREFIX + labName, studentId + ":" + reportName);
    }

    public void removeReport(String studentId, String labName, String reportName) {
        stringSetRepository.removeValue(STUDENTS_PREFIX + studentId, labName + ":" + reportName);
        stringSetRepository.removeValue(LABS_PREFIX + labName, studentId + ":" + reportName);
    }

    public void removeLab(String labName) {
        stringSetRepository.removeValue("labs", labName);
        Set<String> studentAndReports = stringSetRepository.getValues(LABS_PREFIX + labName);
        if (studentAndReports != null) {
            for (String studentAndReport : studentAndReports) {
                String[] split = studentAndReport.split(":");
                String studentId = split[0];
                String reportName = split[1];
                stringSetRepository.removeValue(STUDENTS_PREFIX + studentId, labName+":"+reportName);
            }
        }
        stringSetRepository.removeValues(LABS_PREFIX + labName);
    }

    public void removeReports(String studentId) {
        Set<String> labAndReports = stringSetRepository.getValues(STUDENTS_PREFIX + studentId);
        if (labAndReports != null) {
            for (String labAndReport : labAndReports) {
                String[] split = labAndReport.split(":");
                String labName = split[0];
                String reportName = split[1];
                stringSetRepository.removeValue(LABS_PREFIX + labName, studentId+":"+reportName);
            }
        }
        stringSetRepository.removeValues(STUDENTS_PREFIX + studentId);

    }

    public Map<String,String> getUserReports(String studentId) {
        Set<String> labAndReports = stringSetRepository.getValues(STUDENTS_PREFIX + studentId);
        Map<String,String> res = new HashMap<>();
        if (labAndReports != null) {
            for (String labAndReport : labAndReports) {
                String[] split = labAndReport.split(":");
                String labName = split[0];
                String reportName = split[1];
                res.put(labName, reportName);
            }
        }
        return res;
    }

    public Map<String,Integer> getSubmittedCounts() {
        Map<String,Integer> res = new HashMap<>();
        Set<String> labs = stringSetRepository.getValues("labs");
        if (labs != null) {
            for (String lab : labs) {
                res.put(lab, 0);
                Set<String> submits = stringSetRepository.getValues(LABS_PREFIX + lab);
                if (submits != null) {
                    res.put(lab, submits.size());
                }
            }
        }
        return res;
    }

    public boolean containsLab(String labName) {
        return stringSetRepository.getValues("labs").contains(labName);
    }

    public Set<String> getSubmittedReports(String labName) {
        return stringSetRepository.getValues(LABS_PREFIX + labName);
    }


}
