package com.kimo.ucenter.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.ucenter.mapper.TokenMapper;
import com.kimo.ucenter.model.po.Token;
import com.kimo.ucenter.model.dto.UserDto;
import com.kimo.ucenter.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Administrator
* @description 针对表【token(令牌)】的数据库操作Service实现
* @createDate 2024-05-25 12:24:15
*/
@Service
public class TokenServiceImpl extends ServiceImpl<TokenMapper, Token>
    implements TokenService {

    @Autowired
    private TokenMapper tokenMapper;

    @Override
    public void executeRevokeAllUserToken(UserDto userDto) {
        var validUserTokens = tokenMapper.findAllValidTokenByUser(userDto.getId());
        if (validUserTokens.isEmpty()){
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
            tokenMapper.updateById(token);
        });
    }

    @Override
    public void executeSaveUserToken(UserDto userDto, String jwt) {
        Token token = Token.builder()
                .userId(userDto.getId())
                .token(jwt)
                .expired(false)
                .revoked(false)
                .build();
        tokenMapper.insert(token);
    }

    @Override
    public Token findByToken(String jwt) {
        return tokenMapper.findByToken(jwt);
    }

    @Override
    public List<Token> findAllValidTokenByUser(Long id) {
        return tokenMapper.findAllValidTokenByUser(id);
    }
}




