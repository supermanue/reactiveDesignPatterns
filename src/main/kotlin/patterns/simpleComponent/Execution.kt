package patterns.simpleComponent

import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

class Execution(private val jobScheduling: JobScheduling, private val storage: Storage, val id: Int) {
    suspend fun executeForever() {
        while (true) {
            val jobId = jobScheduling.nextJob()
            if (jobId != null) {
                val job = storage.get(jobId)!!
                println("Execution $id. Executing job $jobId: ${job.content}")
                delay((1000 * Math.random().absoluteValue).toLong()) // Simulate some computation time
            } else {
                delay(100) // just wait
            }
        }
    }
}