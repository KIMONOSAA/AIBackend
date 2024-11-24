package com.kimo.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.ucenter.model.dto.UserListDto;
import com.kimo.ucenter.model.po.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Mr.kimo
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-05-16 22:39:37
* @Entity com.yupi.springbootinit.model.entity.User
*/
public interface UserMapper extends BaseMapper<User> {

    int insertUser(User user);
    User findByEmail(String username);
    List<UserListDto> getUserForRolesNodeById(@Param("pageSize") int pageSize,
                                              @Param("offset") int offset);
}




