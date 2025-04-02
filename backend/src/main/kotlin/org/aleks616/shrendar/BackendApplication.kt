package org.aleks616.shrendar

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Component
class NgrokHeaderFilter:OncePerRequestFilter(){
    override fun doFilterInternal(
        request:HttpServletRequest,
        response:HttpServletResponse,
        filterChain:FilterChain
    ){
        response.setHeader("ngrok-skip-browser-warning","true")
        filterChain.doFilter(request,response)
    }
}

@Configuration
class WebConfig:WebMvcConfigurer{
    override fun addCorsMappings(registry:CorsRegistry){
        registry.addMapping("/**")
            .allowedOriginPatterns(
                "https://localhost:[*]",
                "http://localhost:[*]",
                "https://*.ngrok-free.app"
            )
            .exposedHeaders("ngrok-skip-browser-warning")
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
