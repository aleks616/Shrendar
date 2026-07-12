package org.aleks616.shrendar.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun passwordEncoder():BCryptPasswordEncoder=BCryptPasswordEncoder()

    @Bean
    fun filterChain(http:HttpSecurity,tokenBlacklistService:TokenBlacklistService):SecurityFilterChain {
        http.csrf {it.disable()}
        http.sessionManagement {it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)}
        http.authorizeHttpRequests {
            it.requestMatchers(
                "/api/logout",
                "/api/users"
            ).authenticated()
            it.anyRequest().permitAll()
        }
        http.addFilterBefore(
            JwtAuthenticationFilter(tokenBlacklistService),
            UsernamePasswordAuthenticationFilter::class.java
        )
        return http.build()
    }
}