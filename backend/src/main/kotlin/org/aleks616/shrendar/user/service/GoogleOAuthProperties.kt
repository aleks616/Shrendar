package org.aleks616.shrendar.user.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix="google")
class GoogleOAuthProperties {
    var clientIds:List<String> = emptyList()
}
