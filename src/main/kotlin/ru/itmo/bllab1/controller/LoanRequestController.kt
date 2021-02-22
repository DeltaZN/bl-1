package ru.itmo.bllab1.controller

import org.springframework.web.bind.annotation.*
import ru.itmo.bllab1.repository.*
import ru.itmo.bllab1.service.CommunicationService
import ru.itmo.bllab1.service.Notification
import javax.persistence.EntityNotFoundException

data class LoanRequestPayload(
    val userId: Long,
    val sum: Double,
    val percent: Double,
    val loanDays: Int,
)

data class LoanResponse(
    val message: String,
    val id: Long? = null,
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/loan")
@RestController
class LoanRequestController(
    private val loanRequestRepository: LoanRequestRepository,
    private val borrowerRepository: BorrowerRepository,
    private val comms: CommunicationService,
) {

    @GetMapping("/borrower/{id}")
    fun getBorrowerLoans(@PathVariable id: Long): List<LoanRequest> {
        val borrower = borrowerRepository.findById(id).orElseThrow {
            EntityNotFoundException("Borrower with id $id not found!")
        }
        return loanRequestRepository.findLoansByBorrower(borrower)
    }

    @GetMapping("{id}")
    fun getLoan(@PathVariable id: Long): LoanRequest = loanRequestRepository.findById(id).orElseThrow {
        EntityNotFoundException("Loan with id $id not found!")
    }

    @PostMapping
    fun makeLoan(@RequestBody payload: LoanRequestPayload): LoanResponse {
        val borrower = borrowerRepository.findById(payload.userId).orElseThrow {
            EntityNotFoundException("Borrower with id ${payload.userId} not found!")
        }
        val loan = LoanRequest(0, payload.sum, LoanRequestStatus.NEW, payload.percent, payload.loanDays, borrower)
        loanRequestRepository.save(loan)
        comms.broadcastNotificationToManagers(Notification(loan.id, "A loan awaits for approval"))
        return LoanResponse("Wait for loan approval", loan.id)
    }
}