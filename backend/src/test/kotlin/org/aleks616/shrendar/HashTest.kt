package org.aleks616.shrendar

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder



class HashTest {

    @Test
    fun matches(){
        val encoder=BCryptPasswordEncoder()
        val password="dupa123!D"
        val encoded=encoder.encode(password)
        assertThat(encoder.matches(password,encoded)).isTrue()
    }
}