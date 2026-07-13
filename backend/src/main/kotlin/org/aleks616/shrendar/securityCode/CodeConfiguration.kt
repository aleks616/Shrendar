package org.aleks616.shrendar.securityCode

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CodeConfiguration {
    @Bean
    fun registrationCodeStorage()=CodeStorage(expiryMinutes=15,resendLimitMinutes=1)

    @Bean
    fun passwordResetCodeStorage()=CodeStorage(expiryMinutes=10,resendLimitMinutes=5)
}