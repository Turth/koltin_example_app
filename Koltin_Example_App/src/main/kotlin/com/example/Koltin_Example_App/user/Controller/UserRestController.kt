package com.example.Koltin_Example_App.user.Controller

import com.example.Koltin_Example_App.user.DTO.CreateUserDto
import com.example.Koltin_Example_App.user.DTO.UpdateUserDto
import com.example.Koltin_Example_App.user.Entities.UserEntity
import com.example.Koltin_Example_App.user.Service.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@Tag(name = "UserRestController")
@RestController
@RequestMapping("/api")
class UserRestController(private val userService: UserService) {

    @GetMapping("/users")
    fun getAllUsers(): List<UserEntity> =
        userService.findAll()

    @PostMapping("/addUser")
    fun createNewUser(@Valid @RequestBody createUserDto: CreateUserDto): UserEntity =
        userService.save(createUserDto);

    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable(value = "id") userId: Long): ResponseEntity<UserEntity> =
        userService.findById(userId);

    @PatchMapping("/users/{id}")
    fun updateArticleById(@PathVariable(value = "id") userId: Long, @Valid @RequestBody updateUserDto: UpdateUserDto): ResponseEntity<UserEntity> =
        userService.update(userId, updateUserDto);

    @DeleteMapping("/users/{id}")
    fun deleteUserById(@PathVariable(value = "id") userId: Long): ResponseEntity<Void> =
        userService.delete(userId);
}