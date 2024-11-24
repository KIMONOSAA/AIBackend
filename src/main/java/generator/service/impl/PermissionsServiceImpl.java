package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.Permissions;
import generator.service.PermissionsService;
import generator.mapper.PermissionsMapper;
import org.springframework.stereotype.Service;

/**
* @author chen
* @description 针对表【permissions(权限)】的数据库操作Service实现
* @createDate 2024-11-21 20:31:55
*/
@Service
public class PermissionsServiceImpl extends ServiceImpl<PermissionsMapper, Permissions>
    implements PermissionsService{

}




