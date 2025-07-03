package com.projectmgmttool.backend.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Project Management Tool API")
                        .version("1.0")
                        .description("Backend APIs for TaskForge (Jira-like PM Tool)")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("your@email.com")));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-apis")
                .pathsToMatch("/api/**")
                .build();
    }
}
