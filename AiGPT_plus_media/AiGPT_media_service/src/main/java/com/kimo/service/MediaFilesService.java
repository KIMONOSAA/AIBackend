package com.kimo.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.common.BaseResponse;
import com.kimo.model.dto.BinFIleListDto;
import com.kimo.model.dto.UploadFileParamsDto;
import com.kimo.model.dto.UploadFileResultDto;
import com.kimo.model.po.MediaFiles;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
* @author kimo
* @description 针对表【media_files】的数据库操作Service
* @createDate 2024-10-02 18:26:38
*/
public interface MediaFilesService extends IService<MediaFiles> {

    /**
     * 上传文件id
     * @param uploadFileParamsDto 上传文件信息
     * @return 文件信息
     */
    public UploadFileResultDto uploadFile(UploadFileParamsDto uploadFileParamsDto, MultipartFile fileStream, String objectName,HttpServletRequest request) throws IOException;

    /**
     *
     * @param companyId
     * @param fileMd5
     * @param uploadFileParamsDto
     * @param bucket
     * @param objectName
     * @return
     */
    public MediaFiles addMediaFilesToDb(String id,Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);

    /**
     * 检查文件是否存在
     * @param fileMd5 文件的md5
     * @return Boolean false不存在，true存在
     *
     */
    public BaseResponse<Boolean> checkFile(String fileMd5);



    public String getPreviewUrl(String fileMd5);

    /**
     * 检查分块是否存在
     * @param fileMd5  文件的md5
     * @param chunkIndex  分块序号
     * @return Boolean false不存在，true存在
     */
    public BaseResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传分块
     * @param fileMd5  文件md5
     * @param chunk  分块序号
     * @return BaseResponse
     */
    public BaseResponse uploadChunk(String fileMd5, int chunk, InputStream file);


    /**
     * 合并分块
     * @param companyId  机构id
     * @param fileMd5  文件md5
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDto 文件信息
     * @return BaseResponse
     */
    public BaseResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto,HttpServletRequest request);


    MediaFiles findById(String mediaId);

    Wrapper<MediaFiles> getQueryWrapper(BinFIleListDto binFIleListDto, HttpServletRequest request);

    void showMediaPlaySource(HttpServletResponse response, String fileMd5);
}
