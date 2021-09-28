package com.example.Koltin_Example_App.user.Entities

import kotlinx.serialization.Serializable
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Serializable
@Entity
@Table(name = "user")
data class UserEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @get: NotBlank
    val userName: String? = null,

    @get: NotBlank
    val password: String? = null
)