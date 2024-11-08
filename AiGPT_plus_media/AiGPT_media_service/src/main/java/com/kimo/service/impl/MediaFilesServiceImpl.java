package com.kimo.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.kimo.common.BaseResponse;
import com.kimo.common.ErrorCode;
import com.kimo.common.ResultUtils;
import com.kimo.constant.CommonConstant;
import com.kimo.constant.MinioConstant;
import com.kimo.constant.SecurityConstants;
import com.kimo.constant.SqlConstants;
import com.kimo.exception.BusinessException;
import com.kimo.exception.ThrowUtils;
import com.kimo.feignclient.UserClient;
import com.kimo.mapper.MediaFilesMapper;
import com.kimo.model.dto.BinFIleListDto;
import com.kimo.model.dto.UploadFileParamsDto;
import com.kimo.model.dto.UploadFileResultDto;
import com.kimo.model.dto.UserDto;
import com.kimo.model.po.MediaFiles;
import com.kimo.service.MediaFilesService;
import com.kimo.utils.ServletUtils;
import com.kimo.utils.SqlUtils;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
* @author kimo
* @description 针对表【media_files】的数据库操作Service实现
* @createDate 2024-10-02 18:26:38
*/
@Slf4j
@Service
public class MediaFilesServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles>
    implements MediaFilesService {

    @Autowired
    MinioClient minioClient;

    @Autowired
    @Lazy
    MediaFilesService currentProxy;


    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    ServletUtils servletUtils;


    @Autowired
    UserClient userClient;

    //普通文件桶
    @Value("${minio.bucket.files}")
    private String bucket_Files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;


    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat(CommonConstant.DATA);
        String folder = sdf.format(new Date()).replace(MinioConstant.SPECIAL_SYMBOLS_TWO, MinioConstant.SPECIAL_SYMBOLS_ONE)+MinioConstant.SPECIAL_SYMBOLS_ONE;
        return folder;
    }

    //获取文件的md5
    private String getFileMd5(InputStream file) {
        try {
            return DigestUtils.md5Hex(file);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    //获取文件的md5
    private String getFileMd5OfString(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //获取指定扩展名的 MIME 类型
    private String getMimeType(String extension){
        if(extension==null)
            extension = "";
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }
    /**
     * @description 将文件写入minIO
     * @param bucket  桶
     * @param objectName 对象名称
     * @return void
     */
    public boolean addMediaFilesToMinIO(InputStream inputStream, String mimeType, String bucket, String objectName) {
        try {
            // 构建上传参数，文件大小未知，每块大小为 5MB
            PutObjectArgs uploadArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, -1, 5242880) // 使用 5MB 分块大小
                    .contentType(mimeType)
                    .build();

            minioClient.putObject(uploadArgs);
            log.debug("上传文件流到 MinIO 成功, bucket: {}, objectName: {}", bucket, objectName);
            return true;
        } catch (Exception e) {
            log.error("上传文件流到 MinIO 出错, bucket: {}, objectName: {}, 错误原因: {}", bucket, objectName, e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_FILE_ERROR);
        }
    }

    /**
     * @description 将文件写入minIO
     * @param bucket  桶
     * @param objectName 对象名称
     * @return void
     */
    public boolean addMediaFilesToMinIOFile(MultipartFile inputStream, String mimeType, String bucket, String objectName) {

        try (InputStream fis = inputStream.getInputStream()){
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .contentType(inputStream.getContentType())
                    .object(objectName)
                    .stream(fis,fis.available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_FILE_ERROR);
        }
    }

    /**
     * @description 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param uploadFileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @return MediaFiles
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(String id,Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){


        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateTime(LocalDateTime.now());
            mediaFiles.setStatus("002003");
            mediaFiles.setManagerId(companyId);

            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}",mediaFiles.toString());
                throw new BusinessException(ErrorCode.SYSTEM_FILE_ERROR);
            }
            log.debug("保存文件信息到数据库成功,{}",mediaFiles.toString());

        }
        return mediaFiles;
    }

    @Override
    public BaseResponse<Boolean> checkFile(String fileMd5) {
        //查询文件信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //桶
            String bucket = mediaFiles.getBucket();
            //存储目录
            String filePath = mediaFiles.getFilePath();
            //文件流
            InputStream stream = null;
            try {
                stream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(filePath)
                                .build());

                if (stream != null) {
                    //文件已存在
                    return ResultUtils.success(true);
                }
            } catch (Exception e) {

            }
        }
        //文件不存在
        return ResultUtils.success(false);
    }


    @Override
    public BaseResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

        //得到分块文件目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;

        //文件流
        InputStream fileInputStream = null;
        try {
            fileInputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket_video)
                            .object(chunkFilePath)
                            .build());

            if (fileInputStream != null) {
                //分块已存在
                return ResultUtils.success(true);
            }
        } catch (Exception e) {

        }
        //分块未存在
        return ResultUtils.success(false);
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + MinioConstant.SPECIAL_SYMBOLS_ONE + fileMd5.substring(1, 2) + MinioConstant.SPECIAL_SYMBOLS_ONE + fileMd5 + MinioConstant.SPECIAL_SYMBOLS_ONE + MinioConstant.CHUNK + MinioConstant.SPECIAL_SYMBOLS_ONE;
    }



    @Transactional
    @Override
    public UploadFileResultDto uploadFile(UploadFileParamsDto uploadFileParamsDto, MultipartFile fileStream, String objectName,HttpServletRequest request) throws IOException {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);

        Long userId = userDto.getId();
        // 文件名称
        String filename = uploadFileParamsDto.getFilename();
        // 文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 文件 MIME 类型
        String mimeType = getMimeType(extension);

        // 计算文件的 MD5 值和文件大小
        String fileMd5 = getFileMd5(fileStream.getInputStream()); // 需要调整该方法，以支持 InputStream
        long fileSize = uploadFileParamsDto.getFileSize(); // 你需要在上传前获取文件的大小

        // 文件的默认目录
        String defaultFolderPath = getDefaultFolderPath();
        // 存储到 MinIO 中的对象名（带目录）
        if (!StringUtils.isEmpty(objectName)) {
            objectName = defaultFolderPath + fileMd5 + extension;
        }

        // 将文件上传到 MinIO
        boolean success = addMediaFilesToMinIOFile(fileStream, mimeType, bucket_Files, objectName);
        if (!success) {
            throw new BusinessException(ErrorCode.SYSTEM_FILE_ERROR);
        }

        // 将文件信息存储到数据库
        uploadFileParamsDto.setFileSize(fileSize); // 设置文件大小
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb("", userId, fileMd5, uploadFileParamsDto, bucket_Files, objectName);

        // 准备返回数据
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;

    }

    @Override
    public BaseResponse uploadChunk(String fileMd5, int chunk, InputStream fileStream) {
        //得到分块文件的目录路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunk;
        //mimeType
        String mimeType = getMimeType(null);
        //将文件存储至minIO
        boolean b = addMediaFilesToMinIO(fileStream, mimeType, bucket_video, chunkFilePath);
        if (!b) {
            log.debug("上传分块文件失败:{}", chunkFilePath);
            return ResultUtils.error(ErrorCode.FILE_CHUNK_ERROR);
        }
        log.debug("上传分块文件成功:{}",chunkFilePath);
        return ResultUtils.success(true);
    }

    @Override
    //todo
    public BaseResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto,HttpServletRequest request) {
        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        String managerId = userDto.getId().toString();
//        String id = "1794941858414170113";
        //=====获取分块文件路径=====
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //组成将分块文件路径组成 List<ComposeSource>
        List<ComposeSource> sourceObjectList = Stream.iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_video)
                        .object(chunkFileFolderPath.concat(Integer.toString(i)))
                        .build())
                .collect(Collectors.toList());
        //=====合并=====
        //文件名称
        String fileName = uploadFileParamsDto.getFilename();
        //文件扩展名
        String extName = fileName.substring(fileName.lastIndexOf("."));
        //合并文件路径
        String mergeFilePath = getFilePathByMd5(fileMd5, extName);
        try {
            //合并文件
            ObjectWriteResponse response = minioClient.composeObject(
                    ComposeObjectArgs.builder()
                            .bucket(bucket_video)
                            .object(mergeFilePath)
                            .sources(sourceObjectList)
                            .build());
            log.debug("合并文件成功:{}",mergeFilePath);
        } catch (Exception e) {
            log.debug("合并文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return ResultUtils.error(ErrorCode.FILE_MERGE_CHUNKS_ERROR);
        }

        // ====验证md5====
        byte[] mergedFileData = downloadFileFromMinIO(bucket_video, mergeFilePath);
        if (mergedFileData == null) {
            log.debug("下载合并后文件失败, mergeFilePath: {}", mergeFilePath);
            return ResultUtils.error(ErrorCode.FILE_DOWNLOAD_MERGE_CHUNKS_ERROR);
        }

        // 验证 MD5
        try (InputStream newFileInputStream = new ByteArrayInputStream(mergedFileData)) {
            // minio 上文件的 md5 值
            String md5Hex = DigestUtils.md5Hex(newFileInputStream);
            // 比较 md5 值，不一致则说明文件不完整
            if (!fileMd5.equals(md5Hex)) {
                return ResultUtils.error(ErrorCode.FILE_MERGE_CHUNKS_ERROR);
            }
            // 文件大小
            uploadFileParamsDto.setFileSize((long)mergedFileData.length); // 使用字节数组的长度
        } catch (Exception e) {
            log.debug("校验文件失败, fileMd5: {}, 异常: {}", fileMd5, e.getMessage(), e);
            return ResultUtils.error(ErrorCode.FILE_DOWNLOAD_MERGE_CHUNKS_ERROR);
        }

        //文件入库
        currentProxy.addMediaFilesToDb(managerId,companyId,fileMd5,uploadFileParamsDto,bucket_video,mergeFilePath);
        //=====清除分块文件=====
        clearChunkFiles(chunkFileFolderPath,chunkTotal);
        return ResultUtils.success(true);
    }

    @Override
    public MediaFiles findById(String mediaId) {
        QueryWrapper<MediaFiles> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(SqlConstants.CHART_ID, mediaId);
        return mediaFilesMapper.selectOne(queryWrapper);
    }

    @Override
    public Wrapper<MediaFiles> getQueryWrapper(BinFIleListDto binFIleListDto, HttpServletRequest request) {

        String username = servletUtils.getHeader(request, SecurityConstants.AUTHORIZATION_HEADER);
        UserDto userDto = userClient.GobalGetLoginUser(username);
        ThrowUtils.throwIf(userDto == null,ErrorCode.NOT_LOGIN_ERROR);

        QueryWrapper<MediaFiles> queryWrapper = new QueryWrapper<>();
        if (binFIleListDto == null) {
            return queryWrapper;
        }
        if(binFIleListDto.getFilename() != null){
            queryWrapper.eq(SqlConstants.FILENAME,binFIleListDto.getFilename());
        }
        if(binFIleListDto.getFileType() != null){
            queryWrapper.eq(SqlConstants.FILE_TYPE,binFIleListDto.getFileType());
        }
        if(binFIleListDto.getTags() != null){
            queryWrapper.eq(SqlConstants.FILE_TAGS,binFIleListDto.getTags());
        }
        queryWrapper.eq(SqlConstants.MANAGER_ID,userDto.getId());

        String sortField = binFIleListDto.getSortField();
        String sortOrder = binFIleListDto.getSortOrder();
        // 拼接查询条件
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;

    }

//    @Override
//    public void showMediaPlaySource(HttpServletResponse response, String fileMd5) {
//        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
//        if (mediaFiles != null) {
//            //桶
//            String bucket = mediaFiles.getBucket();
//            //存储目录
//            String filePath = mediaFiles.getFilePath();
//            try {
//
//                StatObjectResponse statObjectResponse = minioClient.statObject(
//                        StatObjectArgs.builder().bucket(bucket).object(filePath).build());
//
//
//                InputStream stream = minioClient.getObject(
//                        GetObjectArgs.builder()
//                                .bucket(bucket)
//                                .object(filePath)
//                                .build());
//                IOUtils.copy(stream,response.getOutputStream());
//
//                response.addHeader("Content-Type", "video/mp4;charset=utf-8");
//
////            response.flushBuffer();
//
////            // 关闭流，此处为示例，流关闭最好放在finally块。
//                stream.close();
//            } catch (Exception e) {
//                System.out.println("Error occurred: " + e);
//            }finally {
//
//            }
//        }
//
//    }
@Override
public void showMediaPlaySource(HttpServletResponse response, String fileMd5) {
    MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
    if (mediaFiles != null) {
        String bucket = mediaFiles.getBucket();
        String filePath = mediaFiles.getFilePath();
        InputStream stream = null;

        try {
            // 检查对象状态
            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucket).object(filePath).build());

            // 获取对象流
            stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(filePath)
                            .build());


            IOUtils.copy(stream, response.getOutputStream());
            response.addHeader("Content-Type", "audio/mpeg;charset=utf-8");
            response.flushBuffer(); // 确保所有数据都已写入
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 设置错误状态码
        } finally {
            // 关闭流
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的字节数组
     */
    public byte[] downloadFileFromMinIO(String bucket, String objectName) {
        try (InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // 复制输入流到字节数组输出流
            IOUtils.copy(stream, outputStream);
            return outputStream.toByteArray(); // 返回下载的文件内容
        } catch (Exception e) {
            log.error("下载文件失败, bucket: {}, objectName: {}, 异常: {}", bucket, objectName, e.getMessage(), e);
            return null;
        }
    }
    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + MinioConstant.SPECIAL_SYMBOLS_ONE + fileMd5.substring(1,2) + MinioConstant.SPECIAL_SYMBOLS_ONE + fileMd5 + MinioConstant.SPECIAL_SYMBOLS_ONE +fileMd5 +fileExt;
    }

    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal){

        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(chunkTotal)
                    .map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(MinioConstant.VIDEO_TYPE).objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清楚分块文件失败,chunkFileFolderPath:{}",chunkFileFolderPath,e);
        }
    }


    public String getPreviewUrl(String fileMd5){
        //查询文件信息
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //桶
            String bucket = mediaFiles.getBucket();
            //存储目录
            String filePath = mediaFiles.getFilePath();
            //文件流
            GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs.builder().bucket(bucket).object(filePath).method(Method.GET).build();
            try {
                return minioClient.getPresignedObjectUrl(build);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
        return null;
    }


}




