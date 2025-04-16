package app.bankcardmanagementsystem.config;


import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.annotations.OpenAPI31;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPI31
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        Contact contact = new Contact();
        contact.setName("Neket");
        contact.setEmail("nikitaivanovitc@gmail.com");

        License mitLicense = new License().name("Apache 2.0")
                .url("https://github.com/Neket27");

        Info info = new Info()
                .title("Финансовый сервис API")
                .version("1.0")
                .contact(contact)
                .description("Документация по взаимодействию с api сервиса")
                .termsOfService("http://localhost:8080")
                .license(mitLicense);

        return new OpenAPI().info(info);
    }
}