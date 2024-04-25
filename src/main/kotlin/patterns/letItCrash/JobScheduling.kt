package patterns.letItCrash

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import patterns.errorKernel.model.ValidationResult
import patterns.letItCrash.model.JobMetadata
import patterns.letItCrash.model.JobStatus
import java.io.File
import java.util.*
import kotlin.math.absoluteValue


class JobScheduling(storage: Storage) {
    private val validation = Validation(storage)
    private val planning = Planning()

    private val pending = mutableListOf<JobMetadata>()
    private val waitingForScheduling = mutableListOf<JobMetadata>()
    private val scheduled: Queue<JobMetadata> = LinkedList<JobMetadata>()
    private val validationResults = mutableListOf<ValidationResult>()
    private val statesMutex = Mutex()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val stateFilePathName = "src/main/kotlin/patterns/letItCrash/storage/"

    init {
        recoverState(pending, "pending.txt")
        recoverState(waitingForScheduling, "waitingForScheduling.txt")
        recoverState(scheduled, "scheduled.txt")
        println("Starting JobScheduling with ${pending.size} pending tasks, ${waitingForScheduling.size} waiting tasks and ${scheduled.size} scheduled tasks")
    }

    fun start(): List<Deferred<Unit>> {
        //Validation
        val jobValidation = scope.async {
            while (true) {
                validate()
                delay((5000 * Math.random().absoluteValue).toLong()) // Simulate some computation time
            }
        }

        //Planning
        val jobPlanning = scope.async {
            while (true) {
                plan()
                delay((5000 * Math.random().absoluteValue).toLong()) // Simulate some computation time
            }
        }

        return listOf(jobValidation, jobPlanning)
    }

    suspend fun accept(clientId: Int, jobId: Int) {
        val jobMetadata = JobMetadata(clientId, jobId, JobStatus.WAITING)
        statesMutex.lock()
        pending.add(jobMetadata)
        statesMutex.unlock()
        saveStates()
    }

    fun nextJob(): Int? {
        val job: JobMetadata? = scheduled.poll()
        return job?.jobId
    }

    suspend fun getJobValidations(clientId: Int): List<ValidationResult> {
        val jobIterator = validationResults.iterator()
        val validations = mutableListOf<ValidationResult>()
        while (jobIterator.hasNext()) {
            val job = jobIterator.next()
            if (job.clientId == clientId) {
                statesMutex.lock()
                validations.add(job)
                jobIterator.remove()
                statesMutex.unlock()
            }
        }
        return validations.toList()
    }

    fun queueSize(): Int = scheduled.size

    private suspend fun validate() {
        val result = validation.validateAll(pending)
        statesMutex.lock()
        pending.clear()
        result.first.forEach { validationResults.add(it) }
        result.second.forEach { waitingForScheduling.add(it) }
        statesMutex.unlock()
        saveStates()
    }

    private suspend fun plan() {
        val result = planning.scheduleAll(waitingForScheduling)
        statesMutex.lock()
        waitingForScheduling.clear()
        result.forEach { scheduled.add(it) }
        statesMutex.unlock()
        saveStates()
    }

    private suspend fun saveStates(){
        statesMutex.lock()
        saveArrayState(pending, "pending.txt")
        saveArrayState(waitingForScheduling, "waitingForScheduling.txt")
        saveArrayState(scheduled.toList(), "scheduled.txt")
        statesMutex.unlock()
    }

    private fun saveArrayState(data: List<JobMetadata>, name: String) {
        //pending
        val pendingTmp =
            File.createTempFile("tmp", ".txt", File(stateFilePathName))
        data.forEach { element ->
            val jsonData = Json.encodeToString(element)
            pendingTmp.appendText(jsonData + "\n")
        }
        pendingTmp.renameTo(File(stateFilePathName + name))
    }

    private fun recoverState(data: MutableList<JobMetadata>, name: String) {
        try {
            File(stateFilePathName + name).readLines().forEach { line ->
                val metadata = Json.decodeFromString<JobMetadata>(line)
                data.add(metadata)
            }
        } catch (_: Exception) {
        }
    }

    private fun recoverState(data: Queue<JobMetadata>, name: String) {
        try {
            File(stateFilePathName + name).readLines().forEach { line ->
                val metadata = Json.decodeFromString<JobMetadata>(line)
                data.add(metadata)
            }
        } catch (_: Exception) {
        }
    }
}


private class Validation(val storage: Storage) {
    fun validateAll(pending: List<JobMetadata>): Pair<List<ValidationResult>, List<JobMetadata>> {
        val validationResults = mutableListOf<ValidationResult>()
        val validated = mutableListOf<JobMetadata>()
        val jobIterator = pending.iterator()
        while (jobIterator.hasNext()) {
            val jobMetadata = jobIterator.next()
            if (validate(jobMetadata.jobId)) {
                validated.add(jobMetadata)
                validationResults.add(ValidationResult(jobMetadata.jobId, jobMetadata.clientId, "Valid"))
            } else {
                validationResults.add(ValidationResult(jobMetadata.jobId, jobMetadata.clientId, "Invalid"))
                storage.pop(jobMetadata.jobId)
            }
        }
        return Pair(validationResults, validated)
    }


    private fun validate(jobId: Int): Boolean {
        return storage.get(jobId)?.content?.isNotEmpty() ?: false
    }
}


private class Planning {
    fun scheduleAll(pending: List<JobMetadata>): List<JobMetadata> {
        val scheduled = mutableListOf<JobMetadata>()
        val sorted = pending.sortedBy { it.jobId }
        sorted.forEach { scheduled.add(it) }
        return scheduled
    }
}