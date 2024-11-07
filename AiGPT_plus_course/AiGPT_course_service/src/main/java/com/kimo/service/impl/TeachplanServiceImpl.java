package com.kimo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;
import com.kimo.mapper.TeachplanMapper;
import com.kimo.mapper.TeachplanMediaMapper;
import com.kimo.model.dto.BindTeachplanMediaDto;
import com.kimo.model.dto.SaveTeachplanDto;
import com.kimo.model.dto.TeachplanDto;
import com.kimo.model.dto.TeachplanListDto;
import com.kimo.model.po.Teachplan;
import com.kimo.model.po.TeachplanMedia;
import com.kimo.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Mr.kimo
 */
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan>
    implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Transactional
    @Override
    public TeachplanMedia associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //教学计划id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if(teachplan==null){
            throw new BusinessException(ErrorCode.VIDEO_FOUND_ERROR);
        }
        teachplan.setTotalTime(bindTeachplanMediaDto.getTotalTime());
        //课程id
        Long courseId = teachplan.getCourseId();

        //先删除原来该教学计划绑定的媒资
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getTeachplanId,teachplanId));

        //再添加教学计划与媒资的绑定关系
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFileName(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);
        teachplanMapper.updateById(teachplan);
        return teachplanMedia;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //课程计划id
        Long id = saveTeachplanDto.getId();
        //修改课程计划
        if(id!=null){
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachplanDto,teachplan);
            teachplan.setChangeDate(LocalDateTime.now());
            teachplanMapper.updateById(teachplan);
        }else{
            //取出同父同级别的课程计划数量
            Long count = getTeachplanCount(saveTeachplanDto.getCourseId());
            Teachplan teachplanNew = new Teachplan();
            //设置排序号
            teachplanNew.setOrderby(Integer.parseInt(count.toString())+1);
            teachplanNew.setCreateDate(LocalDateTime.now());
            BeanUtils.copyProperties(saveTeachplanDto,teachplanNew);

            teachplanMapper.insert(teachplanNew);

        }
    }

    @Override
    public List<TeachplanListDto> findTeachplanTree(Long courseId) {
        List<TeachplanListDto> teachplanDtos = teachplanMapper.getTeachPlanTreeNodeById(courseId);
        return teachplanDtos;
    }

    private Long getTeachplanCount(Long courseId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId);
        Long count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }

}




