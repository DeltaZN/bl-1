package ru.itmo.bllab1.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.itmo.bllab1.repository.*
import ru.itmo.bllab1.service.CommunicationService
import ru.itmo.bllab1.service.Notification
import ru.itmo.bllab1.service.UserService
import javax.persistence.EntityNotFoundException
import javax.persistence.ManyToOne

data class LoanRequestPayload(
    val sum: Double,
    val percent: Double,
    val loanDays: Int
)

data class LoanResponse(
    val message: String,
    val id: Long? = null
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/loan")
@RestController
class LoanRequestController(
    private val loanRequestRepository: LoanRequestRepository,
    private val borrowerRepository: BorrowerRepository,
    private val comms: CommunicationService,
    private val userService: UserService
) {

    companion object {
        fun mapLoanRequestData(loan: LoanRequest): LoanRequestData =
            LoanRequestData(loan.id, loan.sum, loan.requestStatus, loan.percent, loan.loanDays,
                UserController.mapBorrowerData(loan.borrower))
    }

    data class LoanRequestData(
        val id: Long,
        val sum: Double,
        val requestStatus: LoanRequestStatus,
        val percent: Double,
        val loanDays: Int,
        val borrower: UserController.BorrowerData,
    )

    @GetMapping("/borrower/{id}")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED', 'MANAGER', 'ADMIN')")
    fun getBorrowerLoans(@PathVariable id: Long): List<LoanRequestData> {
        userService.checkBorrowerAuthority(id)
        val borrower = borrowerRepository.findById(id).orElseThrow {
            EntityNotFoundException("Borrower with id $id not found!")
        }
        return loanRequestRepository.findLoansByBorrower(borrower)
            .map { l -> mapLoanRequestData(l) }
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED', 'MANAGER', 'ADMIN')")
    fun getLoan(@PathVariable id: Long): LoanRequestData {
        val request = loanRequestRepository.findById(id).orElseThrow {
            EntityNotFoundException("Loan with id $id not found!")
        }
        userService.checkBorrowerAuthority(request.borrower.id)
        return mapLoanRequestData(request)
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED')")
    fun makeLoan(@RequestBody payload: LoanRequestPayload): LoanResponse {
        val borrower = userService.getUserFromAuth().borrower!!
        val loan = LoanRequest(0, payload.sum, LoanRequestStatus.NEW, payload.percent, payload.loanDays, borrower)
        loanRequestRepository.save(loan)
        comms.broadcastNotificationToManagers(Notification(loan.id, "A loan awaits for approval"))
        return LoanResponse("Wait for loan approval", loan.id)
    }
}