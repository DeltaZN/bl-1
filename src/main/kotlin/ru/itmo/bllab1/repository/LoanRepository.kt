package ru.itmo.bllab1.repository

import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import javax.persistence.*

enum class LoanStatus {
    NORMAL,
    EXPIRED,
    CLOSED,
}

@Entity
class Loan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var sum: Double = 0.0,
    var percent: Double = 0.0,
    var startDate: LocalDateTime = LocalDateTime.now(),
    var finishDate: LocalDateTime = LocalDateTime.now(),
    var loanStatus: LoanStatus = LoanStatus.NORMAL,
    @ManyToOne
    var borrower: Borrower = Borrower(),
    @ManyToOne
    var approver: Manager = Manager(),
    var loanReqId: Long = 0,
)

interface LoanRepository : CrudRepository<Loan, Long> {
    fun findLoansByLoanStatus(status: LoanStatus): List<Loan>
    fun findLoansByBorrower(borrower: Borrower): List<Loan>
}