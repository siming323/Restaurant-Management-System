package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.exception.FireNameIsNullException;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
/*@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用Controller接口")*/
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传接口
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传:{}",file);
        String originalFilename = file.getOriginalFilename();
        try {
            if (originalFilename!=null){
                // 获得原始文件名之后，截取原始文件名的后缀
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                // 利用UUID构造新的文件名称
                String objectName = UUID.randomUUID().toString() + extension;
                // 文件的请求路径
                String filePath = aliOssUtil.upload(file.getBytes(), objectName);
                return Result.success(filePath);
            }else {
                throw new FireNameIsNullException(MessageConstant.FILE_NAME_IS_NULL);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("文件上传失败:{}",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
