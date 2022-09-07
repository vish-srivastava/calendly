package com.calendy

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan
class KotlinMainApplication

fun main(args: Array<String>) {
    SpringApplication.run(KotlinMainApplication::class.java, *args)
}