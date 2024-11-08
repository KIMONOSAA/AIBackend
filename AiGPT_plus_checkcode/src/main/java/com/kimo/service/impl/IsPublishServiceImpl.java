package com.kimo.service.impl;

import com.kimo.event.RegistrationCompleteEvent;
import com.kimo.model.UserDto;
import com.kimo.service.IsPublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
/**
 * @author Mr.kimo
 */
@Service
public class IsPublishServiceImpl implements IsPublishService {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public void Publish(Long key, String code,String email) {
        publisher.publishEvent(new RegistrationCompleteEvent(key, code,email));
    }

    public String generateVerification(){
        return String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
    }
}
