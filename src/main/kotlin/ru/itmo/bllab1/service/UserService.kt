package ru.itmo.bllab1.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.itmo.bllab1.auth.ERole
import ru.itmo.bllab1.auth.UserDetailsImpl
import ru.itmo.bllab1.repository.EUser
import ru.itmo.bllab1.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun getCurrentUserId(): Long = (SecurityContextHolder.getContext().authentication.principal as UserDetailsImpl).id

    fun getUserFromAuth(): EUser = userRepository.findById(getCurrentUserId())
        .orElseThrow { UsernameNotFoundException("User not found - ${getCurrentUserId()}") }

    fun checkBorrowerAuthority(ownerId: Long) {
        val accessor = getUserFromAuth()
        if (accessor.roles.any { r -> r.name == ERole.ROLE_ADMIN || r.name == ERole.ROLE_MANAGER })
            return
        val borrower = accessor.borrower
        if (ownerId != borrower?.id)
            throw IllegalAccessException("Access denied")
    }

    fun checkManagerAuthority(ownerId: Long) {
        val accessor = getUserFromAuth()
        if (accessor.roles.any { r -> r.name == ERole.ROLE_ADMIN })
            return
        val manager = accessor.manager
        if (ownerId != manager?.id)
            throw IllegalAccessException("Access denied")
    }
}