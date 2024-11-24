package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.Roles;
import generator.service.RolesService;
import generator.mapper.RolesMapper;
import org.springframework.stereotype.Service;

/**
* @author chen
* @description 针对表【roles(角色)】的数据库操作Service实现
* @createDate 2024-11-21 20:31:55
*/
@Service
public class RolesServiceImpl extends ServiceImpl<RolesMapper, Roles>
    implements RolesService{

}




