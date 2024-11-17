package com.kimo.ucenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.kimo.ucenter.model.po.Token;
import com.kimo.ucenter.model.dto.UserDto;

import java.util.List;

/**
 * @Author: Mr.kimo
 * @Date: 10:48
 * @return:
 * @Param:
 * @Description:
 */
public interface TokenService extends IService<Token> {

    void executeRevokeAllUserToken(UserDto userDto);

    void executeSaveUserToken(UserDto userDto,String jwt);

    Token findByToken(String jwt);

    List<Token> findAllValidTokenByUser(Long id);
}
