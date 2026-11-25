package es.safareyes.smartchef.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    // datos generales que aparecen en la cabecera de Swagger UI
    @Bean
    public OpenAPI apiSmartChef() {
        return new OpenAPI().info(new Info()
                .title("API de SmartChef")
                .version("1.0")
                .description("Recetas, despensa, lista de la compra, colecciones, "
                        + "registro de comidas e informes"));
    }
}