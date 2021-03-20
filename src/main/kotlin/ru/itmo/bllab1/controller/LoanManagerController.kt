package ru.itmo.bllab1.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.itmo.bllab1.repository.*
import ru.itmo.bllab1.service.CommunicationService
import ru.itmo.bllab1.service.MoneyService
import ru.itmo.bllab1.service.Notification
import ru.itmo.bllab1.service.UserService
import java.time.LocalDateTime
import javax.persistence.EntityNotFoundException

data class ManageLoanRequest(
    val loanReqId: Long,
)

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/manage/loan/")
@RestController
class LoanManagerController(
    private val loanRequestRepository: LoanRequestRepository,
    private val loanRepository: LoanRepository,
    private val moneyService: MoneyService,
    private val borrowerRepository: BorrowerRepository,
    private val comms: CommunicationService,
    private val userService: UserService,
) {

    data class LoanData(
        val id: Long,
        val sum: Double,
        val percent: Double,
        val startDate: LocalDateTime,
        val finishDate: LocalDateTime,
        val loanStatus: LoanStatus,
        val borrower: UserController.BorrowerData,
        val approver: UserController.ManagerData,
        val loanReqId: Long,
    )

    companion object {
        fun mapLoanData(loan: Loan): LoanData =
            LoanData(
                loan.id, loan.sum, loan.percent, loan.startDate, loan.finishDate,
                loan.loanStatus, UserController.mapBorrowerData(loan.borrower),
                UserController.mapManagerData(loan.approver), loan.loanReqId
            )
    }

    @PostMapping("/approve")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun approveLoan(@RequestBody payload: ManageLoanRequest): MessageIdResponse {
        val manager = userService.getUserFromAuth().manager!!
        val loanRequest = loanRequestRepository.findById(payload.loanReqId).orElseThrow {
            EntityNotFoundException("Loan request with id ${payload.loanReqId} not found!")
        }

        val loan = Loan(
            0,
            loanRequest.sum,
            loanRequest.percent,
            finishDate = LocalDateTime.now().plusDays(loanRequest.loanDays.toLong()),
            borrower = loanRequest.borrower,
            approver = manager,
            loanReqId = loanRequest.id
        )
        loanRequest.requestStatus = LoanRequestStatus.APPROVED
        loanRequestRepository.save(loanRequest)
        loanRepository.save(loan)

        moneyService.sendMoney(loanRequest.borrower, loanRequest.sum)

        comms.sendNotificationToBorrower(Notification(loan.id, "Your loan has been approved"), loanRequest.borrower)


        return MessageIdResponse("Loan request approved", loan.id)
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    fun rejectLoan(@RequestBody payload: ManageLoanRequest): MessageIdResponse {
        val loanRequest = loanRequestRepository.findById(payload.loanReqId).orElseThrow {
            EntityNotFoundException("Loan request with id ${payload.loanReqId} not found!")
        }

        loanRequest.requestStatus = LoanRequestStatus.REJECTED
        loanRequestRepository.save(loanRequest)

        comms.sendNotificationToBorrower(
            Notification(loanRequest.id, "Your loan has been rejected"),
            loanRequest.borrower
        )

        return MessageIdResponse("Loan request rejected", loanRequest.id)
    }

    @GetMapping("/borrower/{id}")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED', 'MANAGER', 'ADMIN')")
    fun getBorrowerLoans(@PathVariable id: Long): List<LoanData> {
        userService.checkBorrowerAuthority(id)
        val borrower = borrowerRepository.findById(id).orElseThrow {
            EntityNotFoundException("Borrower with id $id not found!")
        }
        return loanRepository.findLoansByBorrower(borrower)
            .map { l -> mapLoanData(l) }
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAnyRole('BORROWER_CONFIRMED', 'MANAGER', 'ADMIN')")
    fun getLoan(@PathVariable id: Long): LoanData {
        val loan = loanRepository.findById(id).orElseThrow {
            EntityNotFoundException("Loan with id $id not found!")
        }
        userService.checkBorrowerAuthority(loan.borrower.id)
        return mapLoanData(loan)
    }

}