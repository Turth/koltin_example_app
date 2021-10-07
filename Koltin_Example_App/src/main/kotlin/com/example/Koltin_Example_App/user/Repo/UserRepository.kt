package com.example.Koltin_Example_App.user.Repo

import com.example.Koltin_Example_App.user.Entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, Long>