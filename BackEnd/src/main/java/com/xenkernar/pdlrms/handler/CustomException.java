package com.xenkernar.pdlrms.handler;

import lombok.Data;

@Data
public class CustomException extends RuntimeException{
    private Integer errCode;
    public CustomException(Integer errCode,String message) {
        super(message);
        this.errCode = errCode;
    }
}
