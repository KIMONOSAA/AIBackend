package com.kimo.model.dto;

import com.kimo.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
/**
 * @author Mr.kimo
 */
@Data
public class BinFIleListDto extends PageRequest implements Serializable {
    /**
     * 文件名称
     */
    private String filename;

    /**
     * 文件类型（图片、文档，视频）
     */
    private String fileType;

    /**
     * 标签
     */
    private String tags;
}
