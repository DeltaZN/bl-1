package ru.itmo.bllab1

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.security.crypto.password.PasswordEncoder
import ru.itmo.bllab1.auth.ERole
import ru.itmo.bllab1.repository.*

@SpringBootApplication
class BlLab1Application: SpringBootServletInitializer(){
	override fun configure(builder: SpringApplicationBuilder): SpringApplicationBuilder {
		return builder.sources(BlLab1Application::class.java)
	}
}
//class BlLab1Application(
//	private val roleRepository: RoleRepository,
//	private val userRepository: UserRepository,
//	private val encoder: PasswordEncoder,
//) : CommandLineRunner {
//	override fun run(vararg args: String?) {
//		val roleBorrower = Role(name = ERole.ROLE_BORROWER)
//		val roleBorrowerConfirmed = Role(name = ERole.ROLE_BORROWER_CONFIRMED)
//		val roleManager = Role(name = ERole.ROLE_MANAGER)
//		val roleAdmin = Role(name = ERole.ROLE_ADMIN)
//		roleRepository.save(roleAdmin)
//		roleRepository.save(roleManager)
//		roleRepository.save(roleBorrower)
//		roleRepository.save(roleBorrowerConfirmed)
//		val admin = EUser(0, "admin", encoder.encode("666666"), null, null, setOf(roleAdmin))
//		userRepository.save(admin)
//	}
//}

fun main(args: Array<String>) {
	runApplication<BlLab1Application>(*args)
}
