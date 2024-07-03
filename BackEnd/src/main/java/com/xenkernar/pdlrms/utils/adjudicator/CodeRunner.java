package com.xenkernar.pdlrms.utils.adjudicator;

import com.xenkernar.pdlrms.entity.TestCase;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import com.xenkernar.pdlrms.properties.AdjudicatorProperties;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@EnableConfigurationProperties(AdjudicatorProperties.class)
public class CodeRunner {

    private static final String ENV_ERROR = "测试环境错误";

    private final AdjudicatorProperties adjudicatorProperties;

    public CodeRunner(AdjudicatorProperties adjudicatorProperties){
        this.adjudicatorProperties = adjudicatorProperties;
    }

    private final ExecutorService cacheExecutor = Executors.newCachedThreadPool();


    private String runCases(String cmd, List<TestCase> testCases) throws IOException, InterruptedException {
        StringBuilder detail = new StringBuilder();
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(1);
        AtomicReference<String> cmdResult = new AtomicReference<>("");
        ProcessBuilder processBuilder = new ProcessBuilder(cmd.split(" "));
        processBuilder.redirectErrorStream(true); // 合并标准错误和标准输出
        Process process = processBuilder.start();
        cacheExecutor.submit(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(adjudicatorProperties.getRunSeparator())) {
                        queue.offer(cmdResult.get());
                    }
                    cmdResult.set(line);
                }
            } catch (IOException e) {
                queue.offer(ENV_ERROR);
            }
        });
        OutputStream outputStream = process.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        if(ENV_ERROR.equals(queue.take())){
            process.destroy();
            return ENV_ERROR;
        }
        for (TestCase testCase: testCases) {
            writer.println("y");
            writer.println(testCase.getInput());
            String caseResult;
            caseResult = queue.poll(adjudicatorProperties.getLimit().getRunMilliseconds(), TimeUnit.MILLISECONDS);
            if (caseResult == null) {
                caseResult = "执行超时";
            }
            if (!isEquals(testCase.getOutput(),caseResult)) {
                detail.append("输入:").append(testCase.getInput()).append(", ");
                detail.append("预期:").append(testCase.getOutput()).append(", ");
                detail.append("实际:").append(caseResult);
                writer.println("n");
                break;
            }
        }
        if (detail.isEmpty()) {
            detail.append("通过");
        }
        process.destroy();
        return detail.toString();
    }

    @SneakyThrows
    private String runCMD(String cmd, List<TestCase> testCases, String containerName){
        String result;
        Future<String> future = cacheExecutor.submit(()-> runCases(cmd, testCases));
        try {
            // 等待任务在指定的时间内完成
            result = future.get(
                    10*(long)Math.max(testCases.size(),1)*adjudicatorProperties.getLimit().getRunMilliseconds(),
                    TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            result = "执行超时";
        } catch (InterruptedException e) {
            // 主线程在等待过程中被中断
            result = "执行中断";
        } catch (ExecutionException e) {
            // 任务在尝试完成过程中抛出了异常
            result = "执行异常";
        } finally {
            future.cancel(true);
            Process process = new ProcessBuilder("docker", "rm","-f", containerName).start();
            process.waitFor();
            process.destroy();
        }
        return result;
    }

    public String run(String language,String srcDir, List<TestCase> testCases){
        if (testCases == null || testCases.isEmpty()){
            return "通过";
        }
        String containerName = UUID.randomUUID().toString();
        String cmd = String.join(" ",
                "docker","run","-i",
                "-v", srcDir+":/src",
                "--memory",adjudicatorProperties.getLimit().getRunMegabytes()+"M",
                "--name",containerName,
                adjudicatorProperties.getLanguages().getDockerImages().get(language));
        return runCMD(cmd, testCases, containerName);
    }

    private static boolean isNumber(String v){
        try {
            Double.parseDouble(v);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static List<Double> parseFloatingNumbers(String input) {
        List<Double> numbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("[-+]?\\d*\\.?\\d+");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            try {
                numbers.add(Double.parseDouble(matcher.group()));
            } catch (NumberFormatException e) {
                numbers.add(0.0);
            }
        }
        return numbers;
    }

    private boolean isEquals(String expect,String actual){
        expect = expect.trim();
        actual = actual.trim();
        if (adjudicatorProperties.getRequireStrictEquality()) {
            if (isNumber(expect) && isNumber(actual)) {
                return Math.abs(Double.parseDouble(expect) - Double.parseDouble(actual)) < adjudicatorProperties.getLimit().getDoubleError();
            }else {
                return expect.equals(actual);
            }
        }
        if (isNumber(expect)){
            List<Double> numbers = parseFloatingNumbers(actual);
            if (numbers.isEmpty()){
                return false;
            }
            for (Double number : numbers){
                if (Math.abs(Double.parseDouble(expect)-number) < adjudicatorProperties.getLimit().getDoubleError()){
                    return true;
                }
            }
        }
        return actual.contains(expect);
    }

    @PreDestroy
    public void destroy() {
        if (!cacheExecutor.isShutdown()) {
            cacheExecutor.shutdown();
            try {
                if (!cacheExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    cacheExecutor.shutdownNow();
                }
            } catch (InterruptedException ex) {
                cacheExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
