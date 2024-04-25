package patterns.heartbeat

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.RemovalListener
import kotlinx.coroutines.*
import patterns.letItCrash.JobScheduling
import patterns.letItCrash.Storage
import patterns.simpleComponent.model.Job
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

class Execution(private val jobScheduling: JobScheduling, private val storage: Storage) {

    private var lastWorkerId = 0
    private var workers: Cache<Int, Worker> =
        CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(10, TimeUnit.SECONDS)
            .removalListener(RemovalListener<Int, Worker>() { notification ->
            println("Worker " + notification.key + " removed")
    }).build()


    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(): List<Deferred<Unit>> {
        println("Starting execution")

        //Escalation
        println("Starting escalation")
        val workerEscalation = scope.async {
            while (true) {
                scale()
                delay(5000) // just wait
            }
        }

        //executors
        println("Starting executors")
        val jobExecutor = scope.async {
            executeForever()
        }

        return listOf(workerEscalation, jobExecutor)
    }


    private fun scale() {
        val size = storage.getAll().size
        if (size > 15) {
            workers.put(lastWorkerId, Worker(lastWorkerId))
            lastWorkerId += 1
            println("Scaling up list of workers to ${workers.size()}")
        } else if (size < 3) {
            try{workers.invalidate(workers.asMap().entries.first().key)}
            catch (_: Exception){}
            finally {println("Scaling down list of workers to ${workers.size()}")}
        }
        println("Number of workers: ${workers.size()}. Queue size: $size")
    }

    private fun executeForever() {
        while (true) {
            val jobId = jobScheduling.nextJob()
            if (jobId != null) {
                val job = storage.pop(jobId)
                if (job != null) {
                    scope.async {
                        val worker = getWorker()
                        worker.execute(job)
                    }
                }
            } else runBlocking {
                delay(1000) }
        }
    }

    private suspend fun getWorker(): Worker {
        fun getIdleWorker(): Worker? = workers.asMap().values.filter { it.status == WorkerStatus.IDLE }.firstOrNull()
        var chosenWorker = getIdleWorker()
        while (chosenWorker == null) {
            delay(100)
            chosenWorker = getIdleWorker()
        }
        return chosenWorker
    }
}

enum class WorkerStatus { IDLE, WORKING }

class Worker(val id: Int, var status: WorkerStatus = WorkerStatus.IDLE) {
    suspend fun execute(job: Job) {
        status = WorkerStatus.WORKING
        println("Worker $id. Executing job: ${job.content}")
        delay((1000 * Math.random().absoluteValue).toLong()) // Simulate some computation time
        status = WorkerStatus.IDLE
    }
}