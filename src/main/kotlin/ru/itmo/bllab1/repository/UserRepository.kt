package ru.itmo.bllab1.repository

import org.springframework.data.repository.CrudRepository
import java.util.*
import javax.persistence.*

@Entity
class EUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var login: String = "",
    var password: String = "",
    @OneToOne(cascade = [CascadeType.ALL])
    var manager: Manager? = null,
    @OneToOne(cascade = [CascadeType.ALL])
    var borrower: Borrower? = null,
    @ManyToMany(fetch = FetchType.EAGER)
    var roles: Set<Role> = emptySet(),
)

interface UserRepository : CrudRepository<EUser, Long> {
    fun findByLogin(login: String): Optional<EUser>
}