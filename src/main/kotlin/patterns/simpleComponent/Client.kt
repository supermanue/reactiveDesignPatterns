package patterns.simpleComponent

import kotlinx.coroutines.delay
import patterns.simpleComponent.model.Job
import kotlin.math.absoluteValue

class Client(private val clientInterface: ClientInterface, private val id: Int) {
    suspend fun produceForever() {
        var cont = 0
        while (true) {
            val job = Job("this is job $cont of client $id")
            println("Client $id. Sending job: ${job.content}")
            clientInterface.submit(job)
            cont++
            delay((1000 * Math.random().absoluteValue).toLong()) // Simulate some computation time
        }
    }
}