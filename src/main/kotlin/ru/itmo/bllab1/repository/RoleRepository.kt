package ru.itmo.bllab1.repository

import org.springframework.data.repository.CrudRepository
import ru.itmo.bllab1.auth.ERole
import java.util.*
import javax.persistence.*

@Entity
data class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Enumerated(EnumType.STRING)
    val name: ERole = ERole.ROLE_BORROWER,
)

interface RoleRepository : CrudRepository<Role, Long> {
    fun findRoleByName(name: ERole): Optional<Role>
}