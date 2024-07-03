package com.xenkernar.pdlrms.utils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;


public class TimeUtils {
    @Getter
    public enum TimeUnit {
        //为单位附带Long数值
        MINUTE(60000L),
        HOUR(3600000L),
        DAY(86400000L);
        private final Long value;
        TimeUnit(Long value) {
            this.value = value;
        }
    }

    private TimeUtils() {}

    public static long timeToMillisecond(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time = 0;
        try {
            time = sdf.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

    public static long timeCycle(long t1,long t2, long base, @NotNull TimeUnit unit){
        long gap = Math.abs(t1 - t2);
        return gap / (base * unit.getValue());
    }
}
