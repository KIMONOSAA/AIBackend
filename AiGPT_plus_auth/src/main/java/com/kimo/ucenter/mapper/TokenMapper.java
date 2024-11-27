package com.kimo.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimo.ucenter.model.po.Token;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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


    // 查询过期的 token，按批次获取，使用 OFFSET 和 LIMIT 进行分页
    @Select("SELECT id FROM token WHERE expired LIMIT #{limit}")
    List<Long> findExpiredTokens(@Param("limit") int limit);

    // 根据 ID 批量删除过期 token
    @Delete("<script>" +
            "DELETE FROM token WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteByIds(@Param("ids") List<Long> ids);
}




