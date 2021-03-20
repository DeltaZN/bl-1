package ru.itmo.bllab1.service

import org.springframework.stereotype.Service
import ru.itmo.bllab1.repository.Borrower
import ru.itmo.bllab1.repository.Manager
import java.time.LocalDateTime

data class Notification(
    val objectId: Long,
    val message: String,
    val time: LocalDateTime = LocalDateTime.now()
)

interface CommunicationService {
    fun sendNotificationToManager(notification: Notification, manager: Manager)
    fun sendNotificationToBorrower(notification: Notification, borrower: Borrower)
    fun broadcastNotificationToManagers(notification: Notification)
}

@Service
class CommunicationServiceStub : CommunicationService {
    override fun sendNotificationToManager(notification: Notification, manager: Manager) {}
    override fun sendNotificationToBorrower(notification: Notification, borrower: Borrower) {}
    override fun broadcastNotificationToManagers(notification: Notification) {}
}