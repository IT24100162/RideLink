package lk.ridelink.driver_vehicle_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI driverVehicleOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink Driver & Vehicle Service API")
                        .version("1.0.0")
                        .description("""
                                Driver operational profiles, vehicle information,
                                availability, service areas, simulated locations,
                                and eligible driver retrieval.
                                """))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}