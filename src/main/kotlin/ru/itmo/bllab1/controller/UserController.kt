package ru.itmo.bllab1.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import ru.itmo.bllab1.auth.ERole
import ru.itmo.bllab1.auth.JwtUtils
import ru.itmo.bllab1.auth.UserDetailsImpl
import ru.itmo.bllab1.repository.*
import ru.itmo.bllab1.service.UserService
import java.util.stream.Collectors
import javax.persistence.EntityNotFoundException

data class RegisterUserRequest(
    val login: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

data class LoginRequest(
    val login: String,
    val password: String
)

data class MessageIdResponse(
    val message: String,
    val id: Long? = null
)

data class AddPassportDataRequest(
    val borrowerId: Long,
    val passportSeriesNumber: String
)

data class JwtResponse(
    val login: String,
    val firstName: String,
    val lastName: String,
    val roles: Collection<String>,
    val accessToken: String,
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api")
@RestController
class UserController(
    private val borrowerRepository: BorrowerRepository,
    private val passportRepository: PassportRepository,
    private val managerRepository: ManagerRepository,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val authenticationManager: AuthenticationManager,
    private val jwtUtils: JwtUtils,
    private val encoder: PasswordEncoder,
    private val userService: UserService
) {

    companion object {
        fun mapBorrowerData(borrower: Borrower): BorrowerData =
            BorrowerData(borrower.id, borrower.firstName, borrower.lastName, borrower.passportData)

        fun mapManagerData(manager: Manager): ManagerData =
            ManagerData(manager.id, manager.firstName, manager.lastName)
    }

    @PostMapping("/signin")
    fun authenticateUser(@RequestBody loginRequest: LoginRequest): ResponseEntity<*>? {
        val authentication: Authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(loginRequest.login, loginRequest.password)
        )
        SecurityContextHolder.getContext().authentication = authentication
        val jwt = jwtUtils.generateJwtToken(authentication)
        val userDetails = authentication.principal as UserDetailsImpl
        val user = userRepository.findByLogin(userDetails.username)
            .orElseThrow { EntityNotFoundException("User not found") }
        return ResponseEntity.ok(JwtResponse(
            user.login,
            user?.borrower?.firstName ?: user?.manager?.firstName ?: "",
            user?.borrower?.lastName ?: user?.manager?.lastName ?: "",
            userDetails.authorities.stream()
                .map { v -> v.authority }
                .collect(Collectors.toList()),
            jwt,
        ))
    }

    data class BorrowerData(
        val id: Long,
        val firstName: String,
        val lastName: String,
        val passportData: PassportData
    )

    @GetMapping("/borrower/{id}")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED', 'MANAGER', 'ADMIN')")
    fun getBorrowerData(@PathVariable id: Long): BorrowerData {
        userService.checkBorrowerAuthority(id)
        val borrower = borrowerRepository.findById(id).orElseThrow {
            EntityNotFoundException("Borrower with id $id not found!")
        }
        return mapBorrowerData(borrower)
    }

    @GetMapping("/borrower")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun getBorrowersData(): Iterable<BorrowerData> = borrowerRepository.findAll()
        .map { b -> mapBorrowerData(b) }

    data class ManagerData(
        val id: Long,
        val firstName: String,
        val lastName: String,
    )

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('ADMIN')")
    fun getManagersData(): Iterable<ManagerData> = managerRepository.findAll()
        .map { m -> mapManagerData(m) }

    @GetMapping("/manager/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun getManagerData(@PathVariable id: Long): ManagerData {
        userService.checkManagerAuthority(id)
        val manager = managerRepository.findById(id).orElseThrow {
            EntityNotFoundException("Manager with id $id not found!")
        }
        return mapManagerData(manager)
    }

    @PostMapping("/borrower/register")
    fun registerBorrower(@RequestBody payload: RegisterUserRequest): MessageIdResponse {
        if (userRepository.findByLogin(payload.login).isPresent)
            throw IllegalStateException("User already registered")
        val passportData = PassportData()
        passportRepository.save(passportData)
        val borrower = Borrower(0, payload.firstName, payload.lastName, passportData)
        val user = EUser(
            0, payload.login, encoder.encode(payload.password), null, borrower,
            setOf(roleRepository.findRoleByName(ERole.ROLE_BORROWER).get())
        )
        borrower.eUser = user
        userRepository.save(user)
        borrowerRepository.save(borrower)
        return MessageIdResponse("Successfully registered user", borrower.id)
    }

    @PostMapping("/manager/register")
    @PreAuthorize("hasAnyRole('ADMIN')")
    fun registerManager(@RequestBody payload: RegisterUserRequest): MessageIdResponse {
        if (userRepository.findByLogin(payload.login).isPresent)
            throw IllegalStateException("User already registered")
        val manager = Manager(0, payload.firstName, payload.lastName)
        val user = EUser(
            0, payload.login, encoder.encode(payload.password), manager, null,
            setOf(roleRepository.findRoleByName(ERole.ROLE_MANAGER).get())
        )
        manager.EUser = user
        userRepository.save(user)
        managerRepository.save(manager)
        return MessageIdResponse("Successfully registered manager", manager.id)
    }

    @PostMapping("/admin/register")
    @PreAuthorize("hasAnyRole('ADMIN')")
    fun registerAdmin(@RequestBody payload: RegisterUserRequest): MessageIdResponse {
        if (userRepository.findByLogin(payload.login).isPresent)
            throw IllegalStateException("User already registered")
        val manager = Manager(0, payload.firstName, payload.lastName)
        val user = EUser(
            0, payload.login, encoder.encode(payload.password), manager, null,
            setOf(roleRepository.findRoleByName(ERole.ROLE_ADMIN).get())
        )
        manager.EUser = user
        userRepository.save(user)
        managerRepository.save(manager)
        return MessageIdResponse("Successfully registered admin", user.id)
    }

    @PostMapping("/borrower/passport")
    @PreAuthorize("hasAnyRole('BORROWER')")
    fun addPassportData(@RequestBody payload: AddPassportDataRequest): MessageIdResponse {
        val borrower = borrowerRepository.findById(payload.borrowerId).orElseThrow {
            EntityNotFoundException("Borrower with id ${payload.borrowerId} not found!")
        }
        val passportData = borrower.passportData
        passportData.passportSeriesAndNumber = payload.passportSeriesNumber
        passportRepository.save(passportData)
        borrower.eUser.roles += roleRepository.findRoleByName(ERole.ROLE_BORROWER_CONFIRMED)
            .orElseThrow { EntityNotFoundException("Role not found???") }
        borrower.passportData = passportData
        userRepository.save(borrower.eUser)
        borrowerRepository.save(borrower)
        return MessageIdResponse("Passport data submitted", passportData.id)
    }
}