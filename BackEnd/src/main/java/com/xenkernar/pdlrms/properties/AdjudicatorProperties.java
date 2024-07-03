package com.xenkernar.pdlrms.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@ConfigurationProperties(prefix = "adjudicator")
@Data
public class AdjudicatorProperties {

    private LanguageConfig languages;
    private CompileConfig compile;
    private LimitConfig limit;
    private Boolean requireStrictEquality;
    private String runSeparator;

    @Data
    public static class LanguageConfig {
        private Set<String> support;
        private Map<String, Set<String>> srcFileSuffix;
        private Map<String, String> dockerImages;
    }

    @Data
    public static class CompileConfig {
        private String srcPath;
    }

    @Data
    public static class LimitConfig {
        private int runMilliseconds;
        private double doubleError;
        private int runMegabytes;
    }
}