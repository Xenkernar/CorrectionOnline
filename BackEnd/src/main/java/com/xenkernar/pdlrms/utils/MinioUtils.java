package com.xenkernar.pdlrms.utils;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class MinioUtils {

    @Resource
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    @Value("${minio.expiry}")
    private Integer expiry;

    private String newFileName(String originalFilename) {
//        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
//        return UUID.randomUUID().toString().replace("-","") + suffix;
        return originalFilename;
    }

    public Map<String,String> upload(String dir, MultipartFile file){

        Map<String,String> res = new HashMap<>();
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder().bucket(bucketName).build();
            if (!minioClient.bucketExists(bucketExistsArgs)) {
                MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder().bucket(bucketName).build();
                minioClient.makeBucket(makeBucketArgs);
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                inputStream.close();
                res.put("error","文件名为空");
                return res;
            }
            String newFileName = newFileName(originalFilename);
            minioClient.putObject(PutObjectArgs.builder()
                    .object(dir+"/"+newFileName)
                    .bucket(bucketName)
                    .contentType(file.getContentType())
                    .stream(inputStream, file.getSize(), -1)
                    .build());
            res.put("fileName",newFileName);
            res.put("dir",dir);
            inputStream.close();
        } catch (Exception e) {
            res.put("error","上传失败: "+e.getMessage());
        }
        return res;
    }

    public Map<String,String> getUrl(String dir, String fileName){
        Map<String,String> res = new HashMap<>();
        try {
            res.put("url", minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(dir+"/"+fileName)
                            .expiry(expiry, TimeUnit.HOURS)
                            .build()));
        } catch (Exception e) {
            res.put("error","获取url失败");
        }
        return res;
    }

    public Map<String,String> delete(String dir,String fileName) {
        Map<String,String> res = new HashMap<>();
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(dir+"/"+fileName)
                .build();
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            res.put("error","删除失败");
        }
        return res;
    }

    public void clearAll() {
        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(bucketName)
                .recursive(true)
                .build();
        Iterable<io.minio.Result<Item>> results = minioClient.listObjects(listObjectsArgs);
        results.forEach(result -> {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(result.get().objectName())
                        .build());
            } catch (Exception e) {
                log.error("minio删除失败: "+e.getMessage());
            }
        });
    }

}
