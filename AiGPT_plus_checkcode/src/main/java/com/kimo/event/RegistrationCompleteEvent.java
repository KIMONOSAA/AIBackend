package com.kimo.event;


import com.kimo.model.UserDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
/**
 * @author Mr.kimo
 */
@Setter
@Getter
public class RegistrationCompleteEvent extends ApplicationEvent {

    private UserDto user;
    private String verificationCode;

    public RegistrationCompleteEvent(UserDto user,String verificationCode) {
        super(user);
        this.user = user;
        this.verificationCode = verificationCode;
    }


}