package com.kimo.controller;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.kimo.common.BaseResponse;
import com.kimo.common.ResultUtils;
import com.kimo.constant.MinioConstant;
import com.kimo.model.dto.BinFIleListDto;
import com.kimo.model.dto.UploadFileParamsDto;
import com.kimo.model.po.MediaFiles;
import com.kimo.service.MediaFilesService;
import io.minio.GetObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;

/**
 *
 * @description 大文件上传接口
 */
@RestController
public class BigFileController {

    @Autowired
    @Lazy
    MediaFilesService mediaFileService;


    /**
     * 文件上传前检查
     * @param fileMd5
     * @return
     * @throws Exception
     */
    @PostMapping("/upload/checkfile")
    public BaseResponse<Boolean> checkfile(
            @RequestParam("fileMd5") String fileMd5,
            HttpServletRequest request
    ) throws Exception {
        BaseResponse<Boolean> booleanRestResponse = mediaFileService.checkFile(fileMd5,request);
        return booleanRestResponse;
    }


    /**
     * 视频分块检查
     *
     *
     */
    @PostMapping("/upload/checkchunk")
    public BaseResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5,
                                            @RequestParam("chunk") int chunk,
                                            HttpServletRequest request
                                            ) throws Exception {
        BaseResponse<Boolean> booleanRestResponse = mediaFileService.checkChunk(fileMd5, chunk,request);
        return booleanRestResponse;
    }


    /**
     * 分块上传
     *
     *
     */
    @PostMapping("/upload/uploadchunk")
    public BaseResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk,
                                    HttpServletRequest request) throws Exception {

        return mediaFileService.uploadChunk(fileMd5, chunk, file.getInputStream(),request);
    }

    /**
     * 合并分块
     *
     *
     */
    @PostMapping("/upload/mergechunks")
    public BaseResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal,HttpServletRequest request) throws Exception {

        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFileType("video");
        uploadFileParamsDto.setTags("课程视频");
        uploadFileParamsDto.setFilename(fileName);

        return mediaFileService.mergechunks(fileMd5,chunkTotal,uploadFileParamsDto,request);

    }

    /**
     * 获取视频列表
     *
     * @param binFIleListDto
     * @return
     */
    @PostMapping("/list/media/data")
    public BaseResponse<Page<MediaFiles>> listAiMasterDataByPage(@RequestBody BinFIleListDto binFIleListDto, HttpServletRequest request) {
        long current = binFIleListDto.getCurrent();
        long size = binFIleListDto.getPageSize();


        Page<MediaFiles> aiRolesPage = mediaFileService.page(new Page<>(current, size),
                mediaFileService.getQueryWrapper(binFIleListDto,request));
        return ResultUtils.success(aiRolesPage);
    }


    @RequestMapping("/show")
    public void download(HttpServletResponse response, @RequestParam String fileMd5){
        mediaFileService.showMediaPlaySource(response,fileMd5);

    }

}
