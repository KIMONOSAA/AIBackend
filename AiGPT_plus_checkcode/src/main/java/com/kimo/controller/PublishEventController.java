package com.kimo.controller;

import com.kimo.model.UserDto;
import com.kimo.service.IsPublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
/**
 * @author Mr.kimo
 */
@RestController
@RequestMapping("/checkcode")
public class PublishEventController {

    @Autowired
    private IsPublishService isPublishService;

    @PostMapping("/publish")
    public void getPublishEvent(@RequestParam Long key,@RequestParam String code,@RequestParam String email){
        isPublishService.Publish(key,code,email);
    }
}
