package com.kimo.feignclient;

import com.kimo.model.dto.chart.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
/**
 * @author Mr.kimo
 */
@Slf4j
@Component
public class UserClientFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable cause) {
        return new UserClient() {


            @Override
            public UserDto GobalGetLoginUser(String request) {
                return null;
            }

            @Override
            public Boolean isAdmin(HttpServletRequest request) {
                return null;
            }

            @Override
            public Boolean updatePoint(Long userId, Long point) {
                return null;
            }
        };
    }
}
