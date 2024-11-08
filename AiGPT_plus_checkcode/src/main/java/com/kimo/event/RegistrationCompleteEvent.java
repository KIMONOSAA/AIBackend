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

    private Long userKey;
    private String verificationCode;
    private String email;

    public RegistrationCompleteEvent(Long userKey,String verificationCode,String email) {
        super(userKey);
        this.email = email;
        this.userKey = userKey;
        this.verificationCode = verificationCode;
    }


}