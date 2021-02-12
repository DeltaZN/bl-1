package ru.itmo.bllab1.controller

import org.springframework.web.bind.annotation.*
import ru.itmo.bllab1.repository.BorrowerRepository
import ru.itmo.bllab1.repository.Loan
import ru.itmo.bllab1.repository.LoanRepository
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException

data class LoanRequest(
    val userId: Long,
    val sum: Double,
    val percent: Double,
    val finishDate: LocalDateTime,
)

data class LoanResponse(
    val message: String,
    val id: Long? = null,
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/loan")
@RestController
class LoanController(
    private val loanRepository: LoanRepository,
    private val borrowerRepository: BorrowerRepository,
) {
    @PostMapping
    fun makeLoan(@RequestBody payload: LoanRequest): LoanResponse {
        val borrower = borrowerRepository.findById(payload.userId).orElseThrow {
            EntityNotFoundException("Borrower with id ${payload.userId} not found!")
        }
        val loan = Loan(0, payload.sum, payload.percent, payload.finishDate, LocalDateTime.now(), borrower)
        loanRepository.save(loan)
        return LoanResponse("Loan approved", loan.id)
    }
}