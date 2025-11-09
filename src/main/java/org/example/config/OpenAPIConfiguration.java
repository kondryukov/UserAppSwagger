package org.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        Server server = new Server();
        server.setUrl("http://localhost:8080");
        server.setDescription("Development");

        Contact myContact = new Contact();
        myContact.setName("Mark Ko");
        myContact.setEmail("MarkKo@gmail.com");

        Info information = new Info()
                .title("User Service API")
                .version("1.0")
                .description("Simple CRUD API for user management")
                .contact(myContact)
                .license(new License().name("Apache 2.0"));
        return new OpenAPI().info(information).servers(List.of(server));
    }
}
