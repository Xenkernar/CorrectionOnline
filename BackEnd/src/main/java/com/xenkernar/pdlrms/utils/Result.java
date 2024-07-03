package com.xenkernar.pdlrms.utils;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@Data
public class Result {

    private Boolean success;
    private Integer code;
    private String message;
    private Map<String,Object> data = new HashMap<>();

    private Result() {}

    public static Result ok() {
        Result res = new Result();
        res.setSuccess(true);
        res.setCode(HttpStatus.OK.value());
        res.setMessage("success");
        return res;
    }

    public static Result error() {
        Result res = new Result();
        res.setSuccess(false);
        res.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage("error");
        return res;
    }

    public Result success(Boolean success) {
        this.setSuccess(success);
        return this;
    }

    public Result message(String message) {
        this.setMessage(message);
        return this;
    }

    public Result code(Integer code) {
        this.setCode(code);
        return this;
    }

    public Result data(Map<String,Object> data) {
        this.setData(data);
        return this;
    }

    public Result data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

}
