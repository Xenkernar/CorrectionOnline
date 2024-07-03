package com.xenkernar.pdlrms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionResult {
    private boolean isCorrect;
    private String detail;
}
