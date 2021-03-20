package ru.itmo.bllab1.repository

import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import javax.persistence.*

enum class LoanRequestStatus {
    NEW,
    APPROVED,
    REJECTED,
}

@Entity
class LoanRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var sum: Double = 0.0,
    var requestStatus: LoanRequestStatus = LoanRequestStatus.NEW,
    var percent: Double = 0.0,
    var loanDays: Int = 0,
    @ManyToOne
    var borrower: Borrower = Borrower()
)

interface LoanRequestRepository : CrudRepository<LoanRequest, Long> {
    fun findLoansByBorrower(borrower: Borrower): List<LoanRequest>
}