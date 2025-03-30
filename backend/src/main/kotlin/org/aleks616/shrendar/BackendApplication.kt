package org.aleks616.shrendar

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class WebConfig:WebMvcConfigurer{
    override fun addCorsMappings(registry:CorsRegistry){
        registry.addMapping("/**")
            .allowedOriginPatterns(
                "http://localhost:[*]",
                "https://*.ngrok-free.app",
                "http://*.ngrok-free.app"
            )
            .allowedOrigins(
                "http://localhost:8080","http://localhost:8081")
            .allowedMethods("GET", "POST", "PUT", "DELETE","OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
            .exposedHeaders("Authorization")
    }
}

@SpringBootApplication
class BackendApplication

fun main(args: Array<String>){
    runApplication<BackendApplication>(*args)
}
