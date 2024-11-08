package com.kimo.ucenter.feignclient;

import com.kimo.ucenter.model.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
//
@FeignClient(value = "checkcode",url = "http://localhost:63090/checkcode", fallbackFactory = CheckCodeClientFactory.class)
public interface CheckCodeClient {

    @PostMapping(value = "/checkcode/publish")
    public void getPublishEvent(@RequestParam("key") Long key,@RequestParam("code") String code,@RequestParam("email") String email);
}