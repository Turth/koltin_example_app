package com.example.Koltin_Example_App.user.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

@Configuration
class AppConfig {
    @Bean
    fun customOpenAPI(): OpenAPI =
            OpenAPI()
                    .components(Components())
                    .info(Info().title("User Service"))

    val redisHost: String = "127.0.0.1"


    val redisPort: Int = 6379

    val redisPassword: String = "password"

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory {
        val config: RedisStandaloneConfiguration = RedisStandaloneConfiguration(redisHost, redisPort)
        config.password = RedisPassword.of(redisPassword)
        return JedisConnectionFactory(config)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any>? {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(jedisConnectionFactory())
        return template
    }
}