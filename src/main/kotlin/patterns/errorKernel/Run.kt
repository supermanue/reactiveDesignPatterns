package patterns.errorKernel

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import java.util.*


class Run {

    fun runForever() = runBlocking {
        val storage = Storage()
        val jobScheduling = JobScheduling(storage)
        val execution = Execution(size = 2, jobScheduling, storage)
        val clientInterface = ClientInterface(storage, jobScheduling)
        val clients = listOf(Client(clientInterface, 1), Client(clientInterface, 2), Client(clientInterface, 3))

        val jobs = LinkedList<Deferred<Unit>>()

        //clients
        clients.forEach { client ->
            jobs.addAll(client.start())
        }

        //jobScheduling
        jobs.addAll(jobScheduling.start())

        //execution
        jobs.addAll(execution.start())

        jobs.forEach { it.await() }
    }
}