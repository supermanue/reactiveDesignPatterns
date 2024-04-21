package patterns.simpleComponent

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*


class Run {

    fun runForever() = runBlocking {
        val storage = Storage()
        val jobScheduling = JobScheduling(storage)
        val clientInterface = ClientInterface(storage, jobScheduling)
        val execution = Execution(jobScheduling, storage, id = 0)

        val jobs = LinkedList<Deferred<Unit>>()

        //clients
        repeat (2){id ->
            var job = async {
                val client = Client(clientInterface, id)
                client.produceForever()
            }
            jobs.add(job)
        }

        //executors
        val job = async {
            execution.executeForever()
        }
        jobs.add(job)

        jobs.forEach { it.await() }
    }
}