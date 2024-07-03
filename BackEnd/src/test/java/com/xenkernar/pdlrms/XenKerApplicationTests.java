package com.xenkernar.pdlrms;

import com.xenkernar.pdlrms.entity.PublishedReport;
import com.xenkernar.pdlrms.entity.ReportTemplate;
import com.xenkernar.pdlrms.repository.PublishedReportRepository;
import com.xenkernar.pdlrms.service.ReportTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.xenkernar.pdlrms.service.AdjudicatorService;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class XenKerApplicationTests {
    @Autowired
    AdjudicatorService adjudicatorService;
    @Autowired
    ReportTemplateService reportTemplateService;
    @Autowired
    PublishedReportRepository publishedReportRepository;
    @Test
    void contextLoads() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        ExecutorService cacheExecutor = Executors.newVirtualThreadPerTaskExecutor();
        File file = new File("D:/Users/Xenkernar/Desktop/reports/202125810201第四周.docx");
        ReportTemplate template = reportTemplateService.get("默认");
        long startTime = System.currentTimeMillis();
        Future<String> future = cacheExecutor.submit(() -> {
            AtomicInteger count = new AtomicInteger();
            for (int i = 0; i < 50; i++) {
                int finalI = i;
                cacheExecutor.submit(() -> {
                    try {
                        PublishedReport lab = publishedReportRepository.findByFileName("程序设计实验-第1周周四.docx").getFirst();
                        adjudicatorService.adjudicateReport(file, "C", template, lab.getTestCases());
                        System.out.println("批改了第"+ (finalI + 1) +"份报告");
                        count.getAndIncrement();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
            while (count.get() < 50) {
                Thread.sleep(1);
            }
            return "success";
        });
        future.get(100, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        System.out.println("测试50份报告耗时: " + (endTime - startTime)/1000 + " 秒");
    }

}
