package ru.itmo.bllab1.controller

import org.springframework.web.bind.annotation.*
import ru.itmo.bllab1.repository.BorrowerRepository
import ru.itmo.bllab1.repository.LoanRepository
import ru.itmo.bllab1.repository.LoanStatus
import ru.itmo.bllab1.service.MoneyService
import javax.persistence.EntityNotFoundException

data class ProcessPaymentRequest(
    val sum: Double,
    val borrowerId: Long,
    val loanId: Long,
)

class ProcessPaymentException(msg: String) : RuntimeException(msg)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/borrower/loan/")
@RestController
class LoanBorrowerController(
    private val loanRepository: LoanRepository,
    private val moneyService: MoneyService,
    private val borrowerRepository: BorrowerRepository,
) {

    @PostMapping("pay")
    fun processPayment(@RequestBody payload: ProcessPaymentRequest): MessageIdResponse {
        val borrower = borrowerRepository.findById(payload.borrowerId).orElseThrow {
            EntityNotFoundException("Borrower with id ${payload.borrowerId} not found!")
        }

        val loan = loanRepository.findById(payload.loanId).orElseThrow {
            EntityNotFoundException("Loan with id ${payload.loanId} not found!")
        }

        if (!moneyService.checkMoneyTransaction(borrower))
            throw ProcessPaymentException("Couldn't process the payment transaction")

        if (loan.loanStatus === LoanStatus.CLOSED)
            throw ProcessPaymentException("Couldn't process CLOSED loan")

        loan.sum -= payload.sum

        return if (loan.sum <= 0.0) {
            loan.loanStatus = LoanStatus.CLOSED
            loanRepository.save(loan)
            MessageIdResponse("You finally closed your loan, CONGRATULATIONS!!!!", loan.id)
        } else {
            loanRepository.save(loan)
            MessageIdResponse("Money processed, but the loan is still opened, remaining sum - ${loan.sum}", loan.id)
        }
    }
}