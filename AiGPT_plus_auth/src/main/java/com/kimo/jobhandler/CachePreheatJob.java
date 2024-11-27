package com.kimo.jobhandler;


import com.kimo.ucenter.mapper.TokenMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Mr.kimo
 */
@Slf4j
@Component
public class CachePreheatJob {


    @Autowired
    private TokenMapper tokenMapper;

    // 设置每次处理的批次大小
    private static final int BATCH_SIZE = 1000;

    @XxlJob("tokenJobHandler")
    public void cachePreheat() {
        log.info("开始执行 Token 过期清理任务");
        int deletedCount = 0;

        while (true) {
            // 查询一批过期的 token
            List<Long> expiredTokenIds = tokenMapper.findExpiredTokens(BATCH_SIZE);

            if (expiredTokenIds.isEmpty()) {
                // 没有更多过期的 token 需要删除，任务完成
                break;
            }

            try {
                // 删除这批 token
                int batchDeletedCount = tokenMapper.deleteByIds(expiredTokenIds);
                deletedCount += batchDeletedCount;
                log.info("成功删除了 {} 条过期的 Token 数据", batchDeletedCount);
            } catch (Exception e) {
                log.error("删除 Token 时出现错误: {}", e.getMessage(), e);
            }
        }

        log.info("Token 过期清理任务完成，共删除了 {} 条数据", deletedCount);
    }
}
