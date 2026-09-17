package com.dtorrez.gym.security.filters;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;

@Configuration
public class CorsFilter implements WebMvcConfigurer {

    // private static final Logger log = LogManager.getLogger(CorsFilter.class);

    @Value("${spring.client.url}")
    private String clientUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        //log.info("****dtn CorsFilter "+ " " + clientUrl);
        String[] urls = clientUrl.split(",");

        registry.addMapping("/**")
                .allowedOrigins(urls)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
