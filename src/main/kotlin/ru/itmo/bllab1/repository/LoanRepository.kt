package ru.itmo.bllab1.repository

import org.springframework.data.repository.CrudRepository
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class Loan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var sum: Double = 0.0,
    var percent: Double = 0.0,
    var startDate: LocalDateTime = LocalDateTime.now(),
    var finishDate: LocalDateTime = LocalDateTime.now(),
    @ManyToOne
    var borrower: Borrower = Borrower(),
)

interface LoanRepository : CrudRepository<Loan, Long>