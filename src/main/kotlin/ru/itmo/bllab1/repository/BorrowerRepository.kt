package ru.itmo.bllab1.repository

import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.*

@Entity
class PassportData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "passport_series_number")
    var passportSeriesAndNumber: String = ""
)

interface PassportRepository : JpaRepository<PassportData, Long>

@Entity
class Borrower(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "first_name")
    var firstName: String = "",
    @Column(name = "last_name")
    var lastName: String = "",
    @OneToOne
    var passportData: PassportData = PassportData(),
    @OneToOne(mappedBy = "borrower")
    var eUser: EUser = EUser()
)

interface BorrowerRepository : JpaRepository<Borrower, Long>