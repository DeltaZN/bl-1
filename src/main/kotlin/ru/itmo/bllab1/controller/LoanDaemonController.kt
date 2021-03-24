package ru.itmo.bllab1.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.itmo.bllab1.service.LoanDaemon

@CrossOrigin(origins = ["*"], maxAge = 3600)
@RequestMapping("/api/daemon/")
@RestController
class LoanDaemonController(
    private val loanDaemon: LoanDaemon
) {
    @PostMapping("/process")
    @PreAuthorize("hasAnyRole('ADMIN')")
    fun processLoans(): MessageIdResponse {
        loanDaemon.processLoans()
        return MessageIdResponse("Loans processed")
    }
}