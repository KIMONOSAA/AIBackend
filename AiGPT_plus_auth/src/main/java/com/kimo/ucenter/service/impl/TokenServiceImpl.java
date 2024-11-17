package com.kimo.ucenter.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimo.common.ErrorCode;
import com.kimo.exception.BusinessException;
import com.kimo.ucenter.mapper.TokenMapper;
import com.kimo.ucenter.model.po.Token;
import com.kimo.ucenter.model.dto.UserDto;
import com.kimo.ucenter.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【token(令牌)】的数据库操作Service实现
* @createDate 2024-05-25 12:24:15
*/
@Service
@Slf4j
public class TokenServiceImpl extends ServiceImpl<TokenMapper, Token>
    implements TokenService {

    @Autowired
    private TokenMapper tokenMapper;

    @Transactional
    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 9:18
     * @return: void
     * @Param: [com.kimo.ucenter.model.dto.UserDto]
     * @Description: 吊销当前用户所有令牌
     */
    public void executeRevokeAllUserToken(UserDto userDto) {
        // 查询有效的用户令牌
        List<Token> validUserTokens = tokenMapper.findAllValidTokenByUser(userDto.getId());
        if (CollectionUtils.isEmpty(validUserTokens)) {
            log.info("未找到 userId： {} 的有效令牌", userDto.getId());
            return;
        }

        // 日志记录开始吊销操作
        log.info("撤销 userId 的所有有效令牌：{}", userDto.getId());

        // 使用批量更新提高性能
        List<Long> tokenIds = validUserTokens.stream().map(Token::getId).collect(Collectors.toList());
        int updatedRows = tokenMapper.batchUpdateStatusToExpiredAndRevoked(tokenIds);
        if (updatedRows != tokenIds.size()) {
            throw new BusinessException(ErrorCode.UPDATE_FAILED, "并非所有令牌都已成功更新");
        }
        // 日志记录完成操作
        log.info("已成功吊销 userId： {} 的 {} 令牌", validUserTokens.size(), userDto.getId());
    }

    @Override
    /**
     * @Author: Mr.kimo
     * @Date: 9:22
     * @return: void
     * @Param: [com.kimo.ucenter.model.dto.UserDto, java.lang.String]
     * @Description: 保存令牌
     */
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




