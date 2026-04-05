package com.sky.admin.operation.service.impl;


import com.sky.admin.operation.service.CommonService;
import com.sky.admin.operation.utils.MinioUtil;
import com.sky.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {

    @Autowired
    private MinioUtil minioUtil;

    @Value("${sky.minio.endpoint}")
    private String endpoint;

    @Value("${sky.minio.bucket-name}")
    private String bucketName;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException("上传文件不能为空");
        }

        try {
            String objectName = minioUtil.upload(file);
            String fileUrl = endpoint + "/" + bucketName + "/" + objectName;
            log.info("文件上传成功，访问地址: {}", fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BaseException("文件上传失败: " + e.getMessage());
        }
    }
}
