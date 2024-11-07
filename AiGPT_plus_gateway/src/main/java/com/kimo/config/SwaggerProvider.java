package com.kimo.config;

import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Mr.kimo
 */
@Component
public class SwaggerProvider implements SwaggerResourcesProvider {

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();
        /**
         * 	/customer为application里面配置的转发,
         * 	后面部分v2/api-docs为固定写法
         * 	2.0表示版本 可以不传任何值
         */
        resources.add(swaggerResource("认证系统", "/auth/v3/api-docs", "auth"));
        resources.add(swaggerResource("AI问题内容系统", "/content/v3/api-docs", "content"));
        resources.add(swaggerResource("验证码系统", "/checkcode/v3/api-docs", "checkcode"));
        resources.add(swaggerResource("可视化系统", "/chart/v3/api-docs", "chart"));
        resources.add(swaggerResource("媒资系统", "/media/v3/api-docs", "media"));
        resources.add(swaggerResource("code系统", "/dictionary/v3/api-docs", "dictionary"));
        resources.add(swaggerResource("练习系统", "/practice/v3/api-docs", "practice"));
        resources.add(swaggerResource("课程系统", "/course/v3/api-docs", "course"));
        resources.add(swaggerResource("订单系统", "/order/v3/api-docs", "course"));
        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location, String version) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion(version);
        return swaggerResource;
    }
}
