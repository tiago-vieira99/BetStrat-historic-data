package com.api.BetStrat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final String BASE_PACKAGE = "com.api.BetStrat.controller";
    private static final String API_TITLE = "Football Betting Strategies Helper API";
    private static final String API_DESCRIPTION = "REST API to get football results info and help on some betting strategies";
    private static final String API_VERSION = "1.0.0";
    private static final String CONTACT_NAME = "Tiago Vieira";
    private static final String CONTACT_GITHUB = "https://github.com/tiago-vieira99/BetStrat";
    private static final String[] AUTH_WHITELIST = {

            // -- swagger ui
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/v2/api-docs",
            "/webjars/**"
    };

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(basePackage(BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build()
                .pathMapping("/")
                .apiInfo(buildApiInfo());
    }

    private ApiInfo buildApiInfo(){
        return new ApiInfoBuilder()
                .title(API_TITLE)
                .description(API_DESCRIPTION)
                .version(API_VERSION)
                .contact(new Contact(CONTACT_NAME, CONTACT_GITHUB, ""))
                .build();
    }
}
