package com.xenkernar.pdlrms.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class SysUser {
    @TableId(type = IdType.INPUT)
    private String id;
    
    @TableField("username")
    private String username;
    private String password;
    private String section;

    @Version
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private String userRole;


}
