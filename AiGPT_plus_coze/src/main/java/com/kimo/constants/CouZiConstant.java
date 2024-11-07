package com.kimo.constants;

import okhttp3.logging.HttpLoggingInterceptor;

public interface CouZiConstant {
    HttpLoggingInterceptor.Level LEVEL_IN_LEVEL = HttpLoggingInterceptor.Level.HEADERS;
    long CONNECT_TIME_OUT = 4500;
    long WRITE_TIME_OUT = 4500;
    long READ_TIME_OUT = 4500;

    String SSE_CONTENT_TYPE = "text/event-stream";
    String DEFAULT_USER_AGENT = "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)";
    String APPLICATION_JSON = "application/json";
    String JSON_CONTENT_TYPE = APPLICATION_JSON + "; charset=utf-8";


    /**
     * 鉴权
     */
    public final static String AUTHORIZATION = "Authorization";
    /**
     * token前缀
     */
    public final static String BEARER = "Bearer ";
}
