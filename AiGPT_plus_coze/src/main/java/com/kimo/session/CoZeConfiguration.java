

package com.kimo.session;



import com.kimo.ICoZeApi;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Getter
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class CoZeConfiguration {


    /**
     * 扣子api接口
     */
    private ICoZeApi coZeApi;
    
    /**
     * 请求客户端
     */
    private OkHttpClient okHttpClient;


    private long connectTimeout = 4500;

    private long writeTimeout = 4500;


    private long readTimeout = 4500;

    
    /**
     * 鉴权密钥
     */
    @NotNull
    private String apiKey;
    
    /**
     * 请求地址
     */
    private String apiHost;

    /**
     * 请求日志打印类型 Level
     * NONE,
     * BASIC,
     * HEADERS,
     * BODY;
     */
    private HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
    
    /**
     * 创建请求工厂
     *
     * @return {@link EventSource.Factory}
     */
    public EventSource.Factory createRequestFactory() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(level);
        okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
        return EventSources.createFactory(okHttpClient);
    }

}
