package com.xenkernar.pdlrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("grade")
public class Grade {
    @TableId(type = IdType.INPUT)
    private String fileName;
    private String studentId;
    private Integer labId;
    private String language;
    private String section;

    private Double score;

    @Version
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
