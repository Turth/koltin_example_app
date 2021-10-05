package com.example.Koltin_Example_App

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class KoltinExampleAppApplication

fun main(args: Array<String>) {
	runApplication<KoltinExampleAppApplication>(*args)
}