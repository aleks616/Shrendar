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
                "/api/user-account/logout",
                "/api/users",
                "/api/album/add",
                "/api/album/edit",
                "/api/album/delete",
                "/api/artist/add",
                "/api/artist/edit",
                "/api/artist/delete",
                "/api/band/add",
                "/api/band/edit",
                "/api/band/delete",
                "/api/band/member-add",
                "/api/band/member-edit",
                "/api/band/member-delete",
                "/api/contribution/**",
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