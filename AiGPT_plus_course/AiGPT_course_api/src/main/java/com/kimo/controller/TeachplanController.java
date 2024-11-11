package com.kimo.controller;



import com.kimo.annotation.PermissionMethod;
import com.kimo.common.BaseResponse;
import com.kimo.common.ResultUtils;
import com.kimo.model.dto.BindTeachplanMediaDto;
import com.kimo.model.dto.SaveTeachplanDto;
import com.kimo.model.dto.TeachplanListDto;
import com.kimo.service.TeachplanService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * @author Mr.kimo
 */
@RestController
public class TeachplanController {

    @Autowired
    private TeachplanService teachplanService;

    /**
     * 获取课程的集合列表
     *
     * @param courseId 课程Id
     * @return 返回课程的集合列表
     */
//    @ApiImplicitParam(value = "courseId",name = "课程Id",required = true,dataType = "Long",paramType = "path")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public BaseResponse<List<TeachplanListDto>> getTreeNodes(@PathVariable Long courseId){
        List<TeachplanListDto> teachplanDtos = teachplanService.findTeachplanTree(courseId);
        return ResultUtils.success(teachplanDtos);
    }

    /**
     * 创建课程教学计划
     *
     * @param teachplan 包含教学计划信息的 DTO 对象
     */
    @PostMapping("/teachplan")
    @PermissionMethod(permission = "course_manager_course_all")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    /**
     * 视频合集（教学计划合集）
     *
     */
    @PostMapping("/teachplan/association/media")
    @PermissionMethod(permission = "course_manager_course_all")
    void associationMedia(@RequestBody BindTeachplanMediaDto bindTeachplanMediaDto){
        teachplanService.associationMedia(bindTeachplanMediaDto);
    }




}
