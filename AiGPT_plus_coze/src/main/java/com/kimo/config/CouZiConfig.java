package com.kimo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
public class CouZiConfig {

    private String token = "pat_7gwklsLnL5KGDMGecF6IuLazLWBNDqwyELV7nGUGrD215fi1D2yjWSKkzSSiVijO";

    private String apiUrl = "https://api.coze.cn/";
}
