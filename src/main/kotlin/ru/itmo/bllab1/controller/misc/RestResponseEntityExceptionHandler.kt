package ru.itmo.bllab1.controller.misc

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.persistence.EntityNotFoundException

data class MessageResponse(
    val message: String,
)

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(value = [EntityNotFoundException::class])
    protected fun handleNotFound(
        ex: Exception, request: WebRequest
    ): ResponseEntity<Any?>? {
        return handleExceptionInternal(ex, MessageResponse(ex.message!!),
            HttpHeaders(), HttpStatus.NOT_FOUND, request)
    }

    @ExceptionHandler(value = [IllegalAccessException::class])
    protected fun handleForbidden(
        ex: Exception, request: WebRequest
    ): ResponseEntity<Any?>? {
        return handleExceptionInternal(ex, "Access denied!",
            HttpHeaders(), HttpStatus.FORBIDDEN, request)
    }
}