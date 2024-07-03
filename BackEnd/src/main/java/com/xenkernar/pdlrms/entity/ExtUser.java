package com.xenkernar.pdlrms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtUser {
    private String id;
    private String originPassword;
    private String password;
    private String username;
    private String code;
}
