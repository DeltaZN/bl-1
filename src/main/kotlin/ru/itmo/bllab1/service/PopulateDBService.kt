package ru.itmo.bllab1.service

import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import ru.itmo.bllab1.auth.ERole
import ru.itmo.bllab1.repository.EUser
import ru.itmo.bllab1.repository.Role
import ru.itmo.bllab1.repository.RoleRepository
import ru.itmo.bllab1.repository.UserRepository

@Service
class PopulateDBService(
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val encoder: PasswordEncoder,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val roleBorrower = Role(name = ERole.ROLE_BORROWER)
        val roleBorrowerConfirmed = Role(name = ERole.ROLE_BORROWER_CONFIRMED)
        val roleManager = Role(name = ERole.ROLE_MANAGER)
        val roleAdmin = Role(name = ERole.ROLE_ADMIN)
        roleRepository.save(roleAdmin)
        roleRepository.save(roleManager)
        roleRepository.save(roleBorrower)
        roleRepository.save(roleBorrowerConfirmed)
        val admin = EUser(0, "admin", encoder.encode("666666"), null, null, setOf(roleAdmin))
        userRepository.save(admin)
    }
}