package com.pros.currencyconversionservice.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author Aldo Navarrete
 * @version 1.0.0
 * @since 2023-12-28
 * Swagger configuration class.
 */
@Configuration
public class DocumentationConfiguration {

    /**
     * OpenAPI bean.
     * @return OpenAPI object.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info().title("Excange Rate Query Service API")
                        .description("The Exchange Rate Service API is a powerful financial tool that provides real-time exchange rate data for various currencies. Developers and businesses can integrate this API into their applications, websites, or financial systems to effortlessly convert currency values, calculate international transaction costs, and stay updated on currency fluctuations. This API offers accurate and up-to-date exchange rate information, making it an essential resource for anyone involved in global finance, e-commerce, or international trade.")
                        .contact(new Contact()
                                .name("Aldo Navarrete")
                                .email("aldo.navarrete@pros.ai")
                                .url("https://pros.com"))
                        .version("1.0.0"))
                ;
    }

}
