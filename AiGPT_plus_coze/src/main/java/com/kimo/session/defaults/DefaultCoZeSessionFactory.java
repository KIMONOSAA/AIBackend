
package com.kimo.session.defaults;



import com.kimo.ICoZeApi;
import com.kimo.session.CoZeConfiguration;
import com.kimo.session.CoZeSession;
import com.kimo.session.CoZeSessionFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;


public class DefaultCoZeSessionFactory implements CoZeSessionFactory {
    private static final Logger log = LoggerFactory.getLogger(DefaultCoZeSessionFactory.class);

    private final CoZeConfiguration cozeConfiguration;

    public DefaultCoZeSessionFactory(CoZeConfiguration cozeConfiguration) {
        this.cozeConfiguration = cozeConfiguration;
    }


    /**
     * 初始化请求配置
     *
     * @return {@link CoZeSession}
     */
    @Override
    public CoZeSession openSession() {
        // 1. 日志配置
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(cozeConfiguration.getLevel());

        // 2. 开启 Http 客户端
        OkHttpClient okHttpClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(cozeConfiguration.getConnectTimeout(), TimeUnit.SECONDS)
                .writeTimeout(cozeConfiguration.getWriteTimeout(), TimeUnit.SECONDS)
                .readTimeout(cozeConfiguration.getReadTimeout(), TimeUnit.SECONDS)
                .build();

        ICoZeApi cozeApi = new Retrofit.Builder()
                .baseUrl(cozeConfiguration.getApiHost())
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .build().create(ICoZeApi.class);
        // 注入配置
        cozeConfiguration.setCoZeApi(cozeApi);
        cozeConfiguration.setOkHttpClient(okHttpClient);
        return new DefaultCoZeSession(cozeConfiguration);
    }

}
