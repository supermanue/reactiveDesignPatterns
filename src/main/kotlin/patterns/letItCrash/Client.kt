package patterns.letItCrash

import kotlinx.coroutines.*
import patterns.simpleComponent.model.Job
import kotlin.math.absoluteValue
import java.io.File

class Client(private val clientInterface: ClientInterface, private val id: Int) {
    private val stateFileName = "src/main/kotlin/patterns/letItCrash/storage/client_$id.txt"

    private var lastJobId = -1
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        lastJobId = try {
            val oldStatus = File(stateFileName).readText()
            oldStatus.toInt()
        } catch (e: Exception){
            0
        }
        println("Starting client $id with lastJobId $lastJobId")
    }

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


    private suspend fun produce() {
        //30% chance of creating an invalid job
        val job = if (Math.random().absoluteValue < 0.7)
            Job("this is job $lastJobId of client $id")
        else
            Job("")
        println("Client $id. Sending job: ${job.content}")
        lastJobId++
        saveState()
        try {
            clientInterface.submit(id, job)
        }
        catch (e: Exception){
            println(e.message + "///" + e.cause)
            println(e.stackTrace)
        }
    }

    private suspend fun getJobStatus() {
        val validations = clientInterface.getJobValidations(id)
  //      validations.forEach { validation -> println("Client $id. Validation for job ${validation.jobId}: ${validation.validationResult}") }
    }

    private fun saveState(){
        File(stateFileName).writeText(lastJobId.toString())
    }

}