package com.example.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author wxz
 * @date 16:16 2023/8/23
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {

    /**
     * @return springfox.documentation.spring.web.plugins.Docket
     * @author wxz
     * @date 16:18 2023/8/23
     */
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2).apiInfo(new ApiInfoBuilder().title("微信支付案例接口文档").build());
    }
}
