package ru.itmo.bllab1.service

import ru.itmo.bllab1.auth.UserDetailsImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.itmo.bllab1.repository.UserRepository

@Service
class UserDetailsServiceImpl(private val userRepository: UserRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails =
            UserDetailsImpl.build(userRepository.findByLogin(username)
                    .orElseThrow { UsernameNotFoundException("Username not found - $username") })
}