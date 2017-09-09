package net.nemerosa.ontrack

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class OntrackApplication

fun main(args: Array<String>) {
    SpringApplication.run(OntrackApplication::class.java, *args)
}
