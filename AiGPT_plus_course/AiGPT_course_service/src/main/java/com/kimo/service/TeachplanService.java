package com.kimo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.model.dto.BindTeachplanMediaDto;
import com.kimo.model.dto.SaveTeachplanDto;
import com.kimo.model.dto.TeachplanListDto;
import com.kimo.model.po.Teachplan;
import com.kimo.model.po.TeachplanMedia;

import java.util.List;

/**
 * @author Mr.kimo
 */
public interface TeachplanService extends IService<Teachplan> {

    /**
     * @description 教学计划绑定媒资
     * @param bindTeachplanMediaDto
     * @return TeachplanMedia
     */
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    void saveTeachplan(SaveTeachplanDto teachplan);

    List<TeachplanListDto> findTeachplanTree(Long courseId);

}
