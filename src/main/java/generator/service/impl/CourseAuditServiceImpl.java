package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.CourseAudit;
import generator.service.CourseAuditService;
import generator.mapper.CourseAuditMapper;
import org.springframework.stereotype.Service;

/**
* @author chen
* @description 针对表【course_audit(审核表)】的数据库操作Service实现
* @createDate 2024-11-25 10:43:38
*/
@Service
public class CourseAuditServiceImpl extends ServiceImpl<CourseAuditMapper, CourseAudit>
    implements CourseAuditService{

}




