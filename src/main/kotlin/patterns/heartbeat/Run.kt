package patterns.heartbeat

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import patterns.letItCrash.Client
import patterns.letItCrash.ClientInterface
import patterns.letItCrash.JobScheduling
import patterns.letItCrash.Storage
import java.util.*


class Run {

    fun runForever() = runBlocking {
        val storage = Storage()
        val jobScheduling = JobScheduling(storage)
        val execution = Execution(jobScheduling, storage)
        val clientInterface = ClientInterface(storage, jobScheduling)
        val clients =  List(3) { it: Int -> Client(clientInterface, it) }

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