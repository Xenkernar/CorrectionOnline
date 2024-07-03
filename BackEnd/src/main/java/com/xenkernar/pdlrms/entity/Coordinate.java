package com.xenkernar.pdlrms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coordinate {
    private Integer tableIndex;
    private Integer rowIndex;
    private Integer columnIndex;
}
