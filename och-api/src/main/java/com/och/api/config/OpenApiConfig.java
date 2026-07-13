package com.och.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openCallHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("openCallHub API")
                        .description("openCallHub 联络中心平台 API 文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("openCallHub")
                                .url("https://github.com/openCallHub"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
