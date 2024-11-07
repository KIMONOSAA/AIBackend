package com.kimo.controller;



import com.alibaba.cloud.commons.lang.StringUtils;
import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.exception.ThrowUtils;
import com.kimo.model.po.MediaFiles;
import com.kimo.service.MediaFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
// @RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    MediaFilesService mediaFileService;

    /**
     * 返回视频url
     *
     *
     */
    @GetMapping("/preview/{mediaId}")
    public String getPlayUrlByMediaId(@PathVariable String mediaId){

        MediaFiles mediaFiles = mediaFileService.findById(mediaId);
        ThrowUtils.throwIf(mediaFiles == null || StringUtils.isEmpty(mediaFiles.getUrl()) ,ErrorCode.FILE_READER_URL_ERROR);

        return mediaFiles.getUrl();

    }


}