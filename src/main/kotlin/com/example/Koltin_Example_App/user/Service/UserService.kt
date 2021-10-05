package com.example.Koltin_Example_App.user.Service

import com.example.Koltin_Example_App.user.DTO.CreateUserDto
import com.example.Koltin_Example_App.user.DTO.UpdateUserDto
import com.example.Koltin_Example_App.user.Entities.UserEntity
import com.example.Koltin_Example_App.user.Repo.UserRepository
import com.example.Koltin_Example_App.user.config.RedisUtil
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import javax.annotation.Resource
import javax.validation.Valid

@Service
class UserService(private  val userRepository: UserRepository) {
    val key = "user"
    val mapper = jacksonObjectMapper()

    //@Value("\${spring.redis.ttl}")
    val testkey: Long = 3600;

    @Resource
    private lateinit var redisUtil: RedisUtil

    fun findAll(): List<UserEntity> =
        userRepository.findAll()

    fun save(@Valid createUserDto: CreateUserDto): UserEntity {
        val userEntity: UserEntity = mapper.convertValue<UserEntity>(createUserDto)
        val result = userRepository.save(userEntity)
        Thread {
            redisUtil.hset(key, userEntity.id.toString(), mapper.writeValueAsString(result), testkey)
        }.start()
        return result
    }

    fun findById(userId: Long): ResponseEntity<UserEntity> {
        val redisResult = redisUtil.hget(key, userId.toString())
        if (redisResult != null) {
            val result = mapper.readValue(redisResult.toString(), UserEntity::class.java)
            return ResponseEntity.ok(result)
        } else
            return userRepository.findById(userId).map { result ->
                redisUtil.hset(key, userId.toString(), mapper.writeValueAsString(result), testkey)
                ResponseEntity.ok(result)
            }.orElse(ResponseEntity.notFound().build())
    }

    fun update(
        userId: Long,
        @Valid updateUserDto: UpdateUserDto
    ): ResponseEntity<UserEntity> {
        redisUtil.hdel(key, userId.toString())
        return userRepository.findById(userId).map { existingUser ->
            val updatedUser: UserEntity = existingUser
                .copy(
                    userName = if (updateUserDto?.userName != null) updateUserDto.userName else existingUser.userName,
                    password = if (updateUserDto?.password != null) updateUserDto.password else existingUser.password
                )

            ResponseEntity.ok().body(userRepository.save(updatedUser))
        }.orElse(ResponseEntity.notFound().build())
    }

    fun delete(articleId: Long): ResponseEntity<Void> {
        redisUtil.hdel(key, articleId.toString())
        return userRepository.findById(articleId).map { article ->
            userRepository.delete(article)
            ResponseEntity<Void>(HttpStatus.OK)
        }.orElse(ResponseEntity.notFound().build())
    }


}