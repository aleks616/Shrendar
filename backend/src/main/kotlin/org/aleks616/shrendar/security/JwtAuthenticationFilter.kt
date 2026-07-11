package org.aleks616.shrendar.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(private val tokenBlacklistService:TokenBlacklistService):OncePerRequestFilter() {
    override fun doFilterInternal(request:HttpServletRequest,response:HttpServletResponse,filterChain:FilterChain) {
        val header=request.getHeader("Authorization")
        if(header!=null&&header.startsWith("Bearer ")) {
            val token=header.substringAfter("Bearer ").trim()
            if(!tokenBlacklistService.isBlacklisted(token)) {
                val subject=JwtUtil.validateToken(token)
                if(subject!=null) {
                    val auth=UsernamePasswordAuthenticationToken(subject,null,emptyList())
                    auth.details=WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication=auth
                }
            }
        }
        filterChain.doFilter(request,response)
    }
}