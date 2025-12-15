package com.chargepoint.csms.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CsmsBackendApplication

fun main(args: Array<String>) {
    runApplication<CsmsBackendApplication>(*args)
}
