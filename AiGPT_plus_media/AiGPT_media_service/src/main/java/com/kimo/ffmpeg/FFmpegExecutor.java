package com.kimo.ffmpeg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FFmpegExecutor {

    private static final Logger log = LoggerFactory.getLogger(FFmpegExecutor.class);

    private static final String FFMPEG_PATH = "/path/to/ffmpeg"; // 替换为你的FFmpeg路径

    /**
     * 执行FFmpeg命令
     *
     * @param commands 要执行的FFmpeg命令
     * @return FFmpeg程序在执行命令过程中产生的各信息，执行出错时返回null
     */
    public static String executeCommand(List<String> commands) {
        if (CollectionUtils.isEmpty(commands)) {
            log.error("--- 指令执行失败，因为要执行的FFmpeg指令为空！ ---");
            return null;
        }

        LinkedList<String> ffmpegCmds = new LinkedList<>(commands);
        ffmpegCmds.addFirst(FFMPEG_PATH); // 设置ffmpeg程序所在路径
        log.info("--- 待执行的FFmpeg指令为：--- " + ffmpegCmds);

        ProcessBuilder builder = new ProcessBuilder(ffmpegCmds);
        builder.redirectErrorStream(true); // 合并标准输出和错误输出流

        try {
            Process ffmpegProcess = builder.start();

            // 读取 FFmpeg 执行结果
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append(System.lineSeparator());
                }
            }

            // 等待FFmpeg进程完成
            int exitCode = ffmpegProcess.waitFor();
            String cmdStr = Arrays.toString(ffmpegCmds.toArray()).replace(",", "");
            if (exitCode == 0) {
                log.info("--- 已执行的FFmpeg命令： --- " + cmdStr + " 已执行完毕, 执行结果：正常");
            } else {
                log.error("--- FFmpeg命令执行失败！ --- 命令：" + cmdStr + " 退出代码：" + exitCode);
                return null;
            }

            return result.toString();

        } catch (Exception e) {
            log.error("--- FFmpeg命令执行出错！ --- 出错信息： " + e.getMessage(), e);
            return null;
        }
    }
}