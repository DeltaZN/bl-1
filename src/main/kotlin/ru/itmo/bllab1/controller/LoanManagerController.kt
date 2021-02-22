package ru.itmo.bllab1.controller

import org.springframework.web.bind.annotation.*
import ru.itmo.bllab1.repository.*
import ru.itmo.bllab1.service.CommunicationService
import ru.itmo.bllab1.service.MoneyService
import ru.itmo.bllab1.service.Notification
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException

data class ManageLoanRequest(
    val managerId: Long,
    val loanReqId: Long,
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/manage/loan/")
@RestController
class LoanManagerController(
    private val managerRepository: ManagerRepository,
    private val loanRequestRepository: LoanRequestRepository,
    private val loanRepository: LoanRepository,
    private val moneyService: MoneyService,
    private val comms: CommunicationService,
) {
    @PostMapping("/approve")
    fun approveLoan(@RequestBody payload: ManageLoanRequest): MessageIdResponse {
        val loanRequest = loanRequestRepository.findById(payload.loanReqId).orElseThrow {
            EntityNotFoundException("Loan request with id ${payload.loanReqId} not found!")
        }

        val manager = managerRepository.findById(payload.managerId).orElseThrow {
            EntityNotFoundException("Manager with id ${payload.managerId} not found!")
        }

        val loan = Loan(0, loanRequest.sum, loanRequest.percent, finishDate = LocalDateTime.now().plusDays(loanRequest.loanDays.toLong()),
            borrower = loanRequest.borrower, approver = manager, loanReqId = loanRequest.id)
        loanRequest.requestStatus = LoanRequestStatus.APPROVED
        loanRequestRepository.save(loanRequest)
        loanRepository.save(loan)

        moneyService.sendMoney(loanRequest.borrower, loanRequest.sum)

        comms.sendNotificationToBorrower(Notification(loan.id, "Your loan has been approved"), loanRequest.borrower)


        return MessageIdResponse("Loan request approved", loan.id)
    }

    @PostMapping("/reject")
    fun rejectLoan(@RequestBody payload: ManageLoanRequest): MessageIdResponse {
        val loanRequest = loanRequestRepository.findById(payload.loanReqId).orElseThrow {
            EntityNotFoundException("Loan request with id ${payload.loanReqId} not found!")
        }

        val manager = managerRepository.findById(payload.managerId).orElseThrow {
            EntityNotFoundException("Manager with id ${payload.managerId} not found!")
        }

        loanRequest.requestStatus = LoanRequestStatus.REJECTED
        loanRequestRepository.save(loanRequest)

        comms.sendNotificationToBorrower(Notification(loanRequest.id, "Your loan has been rejected"), loanRequest.borrower)

        return MessageIdResponse("Loan request rejected", loanRequest.id)
    }
}