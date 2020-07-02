package online.oboz.trip.trip_carrier_advance_payment_api.config;


import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerDocumentationConfig {

    private static String ALLOWED_PATHS = "/.*";

    private static String SECURED_PATHS = "/v1.*";
    private static String SECURED_PATHS2 = "/v1/desktop.*";

    private static String TITLE = "trip-carrier-advance-payment-api";

    private static String DESCRIPTION = "Сервис trip-carrier-advance-payment-api";

    private static String VERSION = "1.0";

    public static final String AUTHORIZATION_HEADER = "Authorization";

    @Bean
    public Docket swaggerSpringfoxDocket() {
        // TODO: специально useDefaultResponseMessages(true/false)
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(true)
            .apiInfo(apiInfo())
            .securityContexts(Lists.newArrayList(securityContext()))
            .securitySchemes(Lists.newArrayList(apiKey()))
            .useDefaultResponseMessages(false);
        docket = docket.select()
            .apis(RequestHandlerSelectors.basePackage("online.oboz"))
            .paths(PathSelectors.regex(ALLOWED_PATHS))
            .build();
        return docket;
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title(TITLE).description(DESCRIPTION).version(VERSION).build();
    }

    private ApiKey apiKey() {
        return new ApiKey("OAUTH2 Bearer", AUTHORIZATION_HEADER, "header");
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .forPaths(PathSelectors.regex(SECURED_PATHS))
            .forPaths(PathSelectors.regex(SECURED_PATHS2))
            .build();
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
            = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Lists.newArrayList(
            new SecurityReference("OAUTH2 Bearer", authorizationScopes));
    }

}
