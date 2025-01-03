package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.until.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonController {
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload_file(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename(); //获取文件原本名字
        String newFileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf(".")); //拼接uuid使名字唯一
        String url = AliOssUtil.uploadFile(newFileName, file.getInputStream()); //返回url地址
        return Result.success(url);
    }
}
