package com.kimo.ucenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.ucenter.model.po.Token;
import com.kimo.ucenter.model.dto.UserDto;

import java.util.List;

/**
* @author Administrator
* @description 针对表【token(令牌)】的数据库操作Service
* @createDate 2024-05-25 12:24:15
*/
public interface TokenService extends IService<Token> {

    void executeRevokeAllUserToken(UserDto userDto);

    void executeSaveUserToken(UserDto userDto,String jwt);

    Token findByToken(String jwt);

    List<Token> findAllValidTokenByUser(Long id);
}
