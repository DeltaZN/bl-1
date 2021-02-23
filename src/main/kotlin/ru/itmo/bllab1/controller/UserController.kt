package ru.itmo.bllab1.controller

import org.springframework.web.bind.annotation.*
import ru.itmo.bllab1.repository.*
import javax.persistence.EntityNotFoundException

data class RegisterUserRequest(
    val firstName: String,
    val lastName: String,
)

data class MessageIdResponse(
    val message: String,
    val id: Long? = null,
)

data class AddPassportDataRequest(
    val userId: Long,
    val passportSeriesNumber: String,
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api")
@RestController
class UserController(
    private val borrowerRepository: BorrowerRepository,
    private val passportRepository: PassportRepository,
    private val managerRepository: ManagerRepository,
) {

    @GetMapping("/borrower/{id}")
    fun getBorrowerData(@PathVariable id: Long): Borrower = borrowerRepository.findById(id).orElseThrow {
        EntityNotFoundException("Borrower with id $id not found!")
    }

    @GetMapping("/borrower")
    fun getBorrowersData(): Iterable<Borrower> = borrowerRepository.findAll()

    @GetMapping("/manager")
    fun getManagersData(): Iterable<Manager> = managerRepository.findAll()

    @GetMapping("/manager/{id}")
    fun getManagerData(@PathVariable id: Long): Manager = managerRepository.findById(id).orElseThrow {
        EntityNotFoundException("Manager with id $id not found!")
    }

    @PostMapping("/borrower/register")
    fun registerBorrower(@RequestBody payload: RegisterUserRequest): MessageIdResponse {
        val passportData = PassportData()
        passportRepository.save(passportData)
        val user = Borrower(0, payload.firstName, payload.lastName, passportData)
        borrowerRepository.save(user)
        return MessageIdResponse("Successfully registered user", user.id)
    }

    @PostMapping("/manager/register")
    fun registerManager(@RequestBody payload: RegisterUserRequest): MessageIdResponse {
        val user = Manager(0, payload.firstName, payload.lastName)
        managerRepository.save(user)
        return MessageIdResponse("Successfully registered manager", user.id)
    }

    @PostMapping("/borrower/passport")
    fun addPassportData(@RequestBody payload: AddPassportDataRequest): MessageIdResponse {
        val borrower = borrowerRepository.findById(payload.userId).orElseThrow {
            EntityNotFoundException("Borrower with id ${payload.userId} not found!")
        }
        val passportData = borrower.passportData
        passportData.passportSeriesAndNumber = payload.passportSeriesNumber
        passportRepository.save(passportData)
        borrower.passportData = passportData
        borrowerRepository.save(borrower)
        return MessageIdResponse("Passport data submitted", passportData.id)
    }
}