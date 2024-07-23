package com.sky.controller.upload;

import com.sky.constant.MessageConstant;
import com.sky.exception.FireNameIsNullException;
import com.sky.result.Result;
import com.sky.service.FileStorageService;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @author siming323
 * @date 2023/10/11 8:35
 */
@RestController
@RequestMapping("/admin/common")
@Api(tags = "文件上传(MINIO)controller接口")
@Slf4j
public class UploadMinioController {
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传接口
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传:{}", file);
        String originalFilename = file.getOriginalFilename();
        try {
            if (originalFilename != null) {
                // 利用UUID构造新的文件名称
                String objectName = UUID.randomUUID().toString() + originalFilename;
                // 文件的请求路径
                String imgFileUrl = fileStorageService.uploadImgFile("", objectName, file.getInputStream());
                log.info("上传图片到Minio, fileId:{}", imgFileUrl);
                return Result.success(imgFileUrl);
            } else {
                throw new FireNameIsNullException(MessageConstant.FILE_NAME_IS_NULL);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("文件上传失败:{}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
