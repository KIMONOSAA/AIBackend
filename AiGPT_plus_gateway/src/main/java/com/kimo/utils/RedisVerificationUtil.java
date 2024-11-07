//package com.kimo.utils;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.kimo.common.ErrorCode;
//import com.kimo.exception.ThrowUtils;
//import com.kimo.ucenter.mapper.UserMapper;
//import com.kimo.ucenter.model.po.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class RedisVerificationUtil {
//
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//
//
//    @Autowired
//    private UserMapper userMapper;
//
//
//    public String generateVerification(){
//        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
//    }
//
//    public boolean verifyCode(String userId,String code,String email,String key){
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("email",email);
//        queryWrapper.eq("id",userId);
//        User user = userMapper.selectOne(queryWrapper);
//        ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR);
//        key = key + userId;
//        String storedCode = redisTemplate.opsForValue().get(key);
//        return code.equals(storedCode);
//    }
//
//    public void deleteCode(String userId,String key){
//        key = key + userId;
//        redisTemplate.delete(key);
//    }
//
//}
