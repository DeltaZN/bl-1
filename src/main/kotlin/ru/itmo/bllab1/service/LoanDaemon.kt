package ru.itmo.bllab1.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.itmo.bllab1.repository.LoanRepository
import ru.itmo.bllab1.repository.LoanStatus
import java.time.LocalDateTime

@Service
class LoanDaemon(
    private val comms: CommunicationService,
    private val loanRepository: LoanRepository,
) {
    @Scheduled(cron = "0 0 5 * * ?")
    fun processLoans() {
        loanRepository.findLoansByLoanStatus(LoanStatus.NORMAL)
            .plus(loanRepository.findLoansByLoanStatus(LoanStatus.EXPIRED))
            .forEach { l ->
            if (LocalDateTime.now() > l.finishDate)
                l.loanStatus = LoanStatus.EXPIRED
            l.sum += l.percent * l.sum
            loanRepository.save(l)
            comms.sendNotificationToBorrower(Notification(l.id, "Interest on the loan is accrued"), l.borrower)
        }

        loanRepository.findLoansByLoanStatus(LoanStatus.EXPIRED).forEach { l ->
            l.percent += 0.05
            loanRepository.save(l)
            comms.sendNotificationToBorrower(Notification(l.id, "Loan percent is increased, you have to pay faster otherwise we'll send collectors!"), l.borrower)
        }
    }
}