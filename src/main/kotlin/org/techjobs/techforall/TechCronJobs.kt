package org.techjobs.techforall

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TechCronJobs

fun main(args: Array<String>) {
	runApplication<TechCronJobs>(*args)
}
