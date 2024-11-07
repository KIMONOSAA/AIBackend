package com.kimo.ucenter.model.po;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Token {


    @TableId(type = IdType.ASSIGN_ID)
    public Long id;


    public String token;


    public boolean revoked;

    public boolean expired;


    public Long userId;
}
