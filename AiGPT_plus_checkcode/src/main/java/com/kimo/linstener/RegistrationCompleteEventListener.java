package com.kimo.linstener;

import com.kimo.common.ErrorCode;
import com.kimo.constant.RedisConstant;
import com.kimo.event.RegistrationCompleteEvent;
import com.kimo.model.UserDto;
import com.kimo.exception.BusinessException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
/**
 * @author Mr.kimo
 */
@Slf4j
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {


    @Autowired
    private RedisTemplate<String,String> redisTemplate;

//    @Autowired
//    private JwtService jwtService;

    @Value("${spring.mail.username}")
    private String masterEmail;
    private final Integer SECONDS = 180;


    @Autowired
    private JavaMailSender mailSender;

    private String theUser;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        theUser = event.getEmail();
        redisTemplate.opsForValue().set(RedisConstant.KEY_UTIL+event.getUserKey(),event.getVerificationCode(), SECONDS, TimeUnit.MINUTES);
        String verificationEmail = event.getVerificationCode();
        try {
            sendVerificationEmail(verificationEmail);
        }catch (MessagingException | UnsupportedEncodingException | jakarta.mail.MessagingException e){
            throw new BusinessException(ErrorCode.EMAIL_ERROR);
        }
        log.info("以下是你的电子邮件验证码："+ verificationEmail);

    }
    public void sendVerificationEmail(String verificationEmail) throws MessagingException, UnsupportedEncodingException, jakarta.mail.MessagingException {
        String subject = "验证你的电子邮件";
        String senderName = "验证码";
        String mailContent = "<p>来自Asouli的问候：</p><br />" +
                "<p>感谢你注册Asouli账户。为确保当前是你本人操作,请你输入此邮件中提到的验证码以完成注册。如你无需注册Asouli账户，请忽略该信息</p>"
                + "<br />" + senderName + "<br />" + verificationEmail + "<br />" + "<p>(此验证码将在发送后1分钟过期)</p>" + "<br />" + "<hr />" +
                "Asouli不会通过邮件让你公开或验证你的密码或银行账号号码。如果你收到带有更新账户信息链接的可疑电子邮件，请不要点击该链接，而是应该将该电子邮件报告给Asouli进行调查" + "<br />" + "<hr />";

        MimeMessage message = mailSender.createMimeMessage();
        var messageHelper = new MimeMessageHelper(message);
        messageHelper.setFrom(masterEmail, senderName);
        messageHelper.setTo(theUser);
        messageHelper.setSubject(subject);
        messageHelper.setText(mailContent, true);
        mailSender.send(message);
    }
}
