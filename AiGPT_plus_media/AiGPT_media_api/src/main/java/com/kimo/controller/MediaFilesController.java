package com.kimo.controller;




import com.kimo.common.BaseResponse;
import com.kimo.common.ResultUtils;
import com.kimo.constant.MinioConstant;
import com.kimo.model.dto.UploadFileParamsDto;
import com.kimo.model.dto.UploadFileResultDto;
import com.kimo.model.po.MediaFiles;
import com.kimo.service.MediaFilesService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 媒资文件管理接口
 * @author kimo
 * @version 1.0
 */
@RestController
public class MediaFilesController {


    @Autowired
    @Lazy
    MediaFilesService mediaFileService;

    /**
     * 图片上传
     *
     *
     */
//    @PostMapping("/files")
//    public PageResult<MediaFiles> list(PageParams pageParams, @RequestBody QueryMediaParamsDto queryMediaParamsDto){
//    Long companyId = 1232141425L;
//    return mediaFileService.queryMediaFiels(companyId,pageParams,queryMediaParamsDto);
//
//    }
    @PostMapping( "/upload/coursefile")
    public BaseResponse<UploadFileResultDto> upload(@RequestParam("filedata") MultipartFile filedata, @RequestParam("objectName") String objectName, HttpServletRequest request) throws IOException {


        UploadFileParamsDto uploadFileParamsDto =
                new UploadFileParamsDto();

        //文件大小
        uploadFileParamsDto.setFileSize(filedata.getSize());
        //图片
        uploadFileParamsDto.setFileType("001001");
        //文件名称
        uploadFileParamsDto.setFilename(filedata.getOriginalFilename());//文件名称
        //文件大小
        long fileSize = filedata.getSize();
        uploadFileParamsDto.setFileSize(fileSize);
//        //创建临时文件
//        File tempFile = File.createTempFile("minio", "temp");
//        //上传的文件拷贝到临时文件
//        filedata.transferTo(tempFile);
//        //文件路径
//        String absolutePath = tempFile.getAbsolutePath();
        //上传的文件拷贝到临时文件
        return ResultUtils.success(mediaFileService.uploadFile(uploadFileParamsDto, filedata, objectName,request));

    }

    @GetMapping("/getPreviewUrl")
    public BaseResponse<String> getPreviewUrl(@RequestParam("filemd5") String filemd5) {
        return ResultUtils.success(mediaFileService.getPreviewUrl(filemd5));
    }

}
