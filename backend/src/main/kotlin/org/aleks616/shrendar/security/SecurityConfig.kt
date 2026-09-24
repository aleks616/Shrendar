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
        http.cors {}
        http.sessionManagement {it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)}
        http.authorizeHttpRequests {
            it.requestMatchers(
                "/api/album/add",
                "/api/album/edit",
                "/api/album/delete",
                "/api/artist/add",
                "/api/artist/edit",
                "/api/artist/delete",
                "/api/artist/favorite",
                "/api/artist/favoriteAll",
                "/api/band/add",
                "/api/band/edit",
                "/api/band/delete",
                "/api/band/favorite",
                "/api/band/member-add",
                "/api/band/member-edit",
                "/api/band/member-delete",
                "/api/contribution/**",
                "/api/event/add",
                "/api/event/edit",
                "/api/event/delete",
                "/api/genre/favorite",
                "/api/user-account/logout",
                "/api/user-account/users",
                "/api/ban/active",
                "/api/ban/withAppeal",
                "/api/ban/{userId}/all",
                "/api/bam/{userId}",
                "/api/ban/by/{modId}",
                "/api/ban/ban",
                "/api/ban/appeal",
                "/api/ban/cancel/{userId}",
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