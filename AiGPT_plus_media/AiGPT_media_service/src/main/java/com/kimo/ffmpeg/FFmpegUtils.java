package com.kimo.ffmpeg;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;
import org.springframework.http.MediaType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.kimo.ffmpeg.FFmpegExecutor.executeCommand;


public class FFmpegUtils {

    /**
     * 视频转换
     *
     * @param fileInput  源视频路径
     * @param fileOutPut 转换后的视频输出路径
     * @param withAudio 是否保留音频；true-保留，false-不保留
     * @param crf 视频质量系数（0-51）
     * @param preset 编码速率
     * @param width 视频宽度；为空则保持源视频宽度
     * @param height 视频高度；为空则保持源视频高度
     */
    public static void convertVideo(File fileInput, File fileOutPut, boolean withAudio, Integer crf, String preset, Integer width, Integer height) {
        // 参数检查
        if (fileInput == null || !fileInput.exists()) {
            throw new BusinessException(ErrorCode.SOURCE_VIDEO_NOT_FOUND);
        }
        if (fileOutPut == null) {
            throw new BusinessException(ErrorCode.VIDEO_PATH_FOUND);
        }

        try {
            if (!fileOutPut.exists()) {
                if (!fileOutPut.createNewFile()) {
                    throw new BusinessException(ErrorCode.NEW_VIDEO_FILE_ERROR);
                }
            }

            String format = getFormat(fileInput);
            if (!isLegalFormat(format, "MP4")) {
                throw new BusinessException(ErrorCode.VIDEO_NOT_FORMAT);
            }

            // 构建FFmpeg命令
            List<String> command = buildCommand(fileInput, fileOutPut, withAudio, crf, preset, width, height);

            // 执行命令
            executeCommand(command);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.VIDEO_CONVERSION_ERROR);
        }
    }

    private static List<String> buildCommand(File fileInput, File fileOutPut, boolean withAudio, Integer crf, String preset, Integer width, Integer height) {
        List<String> command = new ArrayList<>();

        command.add("-i");
        command.add(fileInput.getAbsolutePath());

        if (!withAudio) {
            command.add("-an"); // 不保留音频
        }

        if (width != null && width > 0 && height != null && height > 0) {
            command.add("-s");
            command.add(width + "x" + height); // 设置分辨率
        }

        command.add("-vcodec");
        command.add("libx264");  // 使用H264编码
        command.add("-preset");
        command.add(preset);  // 设置编码速率
        command.add("-crf");
        command.add(crf.toString());  // 设置视频质量系数
        command.add("-y");  // 强制覆盖输出文件

        command.add(fileOutPut.getAbsolutePath());

        return command;
    }

    // 获取视频格式
    private static String getFormat(File file) {
        // 文件名称
        String filename = file.getAbsolutePath();
        // 文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        // 文件 MIME 类型
        return getMimeType(extension);

    }

    private static String getMimeType(String extension){
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

    // 检查视频格式是否合法
    private static boolean isLegalFormat(String format, String validType) {
        return format != null && format.equalsIgnoreCase(validType);
    }
}
