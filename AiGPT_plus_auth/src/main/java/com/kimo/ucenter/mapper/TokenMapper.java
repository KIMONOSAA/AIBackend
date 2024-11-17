package com.kimo.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.ucenter.model.po.Token;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
* @author Mr.kimo
* @description 针对表【token(令牌)】的数据库操作Mapper
* @createDate 2024-05-25 12:24:15
* @Entity generator.domain.Token
*/
@Mapper
@Repository
public interface TokenMapper extends BaseMapper<Token> {

    Token findByToken(String jwt);

    List<Token> findAllValidTokenByUser(Long id);

    int batchUpdateStatusToExpiredAndRevoked(@Param("tokenIds") List<Long> tokenIds);

}




