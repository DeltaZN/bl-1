package ru.itmo.bllab1.auth

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.itmo.bllab1.repository.EUser
import java.util.stream.Collectors


class UserDetailsImpl(
    val id: Long,
    private val login: String,
    @JsonIgnore
    private val password: String,
    private val authorities: Collection<GrantedAuthority>,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    companion object {
        fun build(EUser: EUser): UserDetailsImpl {
            val authorities: List<GrantedAuthority> = EUser.roles.stream()
                .map { role -> SimpleGrantedAuthority(role.name.toString()) }
                .collect(Collectors.toList())
            return UserDetailsImpl(
                EUser.id,
                EUser.login,
                EUser.password,
                authorities
            )
        }
    }

    override fun getPassword(): String = password

    override fun getUsername(): String = login

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
}