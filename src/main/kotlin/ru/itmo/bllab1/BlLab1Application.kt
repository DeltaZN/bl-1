package ru.itmo.bllab1

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
class BlLab1Application: SpringBootServletInitializer() {
	override fun configure(builder: SpringApplicationBuilder): SpringApplicationBuilder {
		return builder.sources(BlLab1Application::class.java)
	}

}

fun main(args: Array<String>) {
	runApplication<BlLab1Application>(*args)
}
