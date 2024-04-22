package patterns.errorKernel

import kotlinx.coroutines.*
import patterns.simpleComponent.model.Job
import kotlin.math.absoluteValue

class Client(private val clientInterface: ClientInterface, private val id: Int) {
    private var lastJobId = 0
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

     fun start(): List<Deferred<Unit>> {
         val produceJob =  scope.async {
            while (true) {
                produce()
                delay((1000 * Math.random().absoluteValue).toLong()) // Simulate some computation time
            }
        }
         val statusJob = scope.async {
             while (true) {
                 getJobStatus()
                 delay((5000 * Math.random().absoluteValue).toLong()) // Simulate some computation time
             }
         }
         return listOf(produceJob, statusJob)
     }


    private fun produce() {
        //30% chance of creating an invalid job
        val job = if (Math.random().absoluteValue < 0.7)
            Job("this is job $lastJobId of client $id")
        else
            Job("")
        println("Client $id. Sending job: ${job.content}")
        clientInterface.submit(id, job)
        lastJobId++
    }

    private fun getJobStatus() {
        val validations = clientInterface.getJobValidations(id)
        validations.forEach { validation -> println("Client $id. Validation for job ${validation.jobId}: ${validation.validationResult}") }
    }


}