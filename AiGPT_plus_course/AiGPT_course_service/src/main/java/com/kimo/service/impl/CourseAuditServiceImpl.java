package com.kimo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.kimo.mapper.CourseAuditMapper;
import com.kimo.model.po.CourseAudit;
import com.kimo.service.CourseAuditService;
import org.springframework.stereotype.Service;

/**
* @author chen
* @description 针对表【course_audit(审核表)】的数据库操作Service实现
* @createDate 2024-11-25 10:32:46
*/
@Service
public class CourseAuditServiceImpl extends ServiceImpl<CourseAuditMapper, CourseAudit>
    implements CourseAuditService {

}




