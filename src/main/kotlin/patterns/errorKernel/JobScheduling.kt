package patterns.errorKernel

import kotlinx.coroutines.*
import patterns.errorKernel.model.Job
import patterns.errorKernel.model.JobMetadata
import patterns.errorKernel.model.ValidationResult
import java.util.LinkedList
import java.util.Queue
import kotlin.math.absoluteValue

class JobScheduling(storage: Storage) {
    private val validation = Validation(storage)
    private val planning = Planning()

    private val pending = mutableListOf<JobMetadata>()
    private val waitingForScheduling = mutableListOf<JobMetadata>()
    private val scheduled: Queue<JobMetadata> = LinkedList<JobMetadata>()
    private val validationResults = mutableListOf<ValidationResult>()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(): List<Deferred<Unit>>  {
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

    fun accept(clientId: Int, jobId: Int) {
        val jobMetadata = JobMetadata(clientId, jobId)
        pending.add(jobMetadata)
    }

    fun nextJob(): Int? {
        val job: JobMetadata? =  scheduled.poll()
        return job?.jobId
    }


    fun getJobValidations(clientId: Int): List<ValidationResult> {
        val jobIterator = validationResults.iterator()
        val validations = mutableListOf<ValidationResult>()
        while (jobIterator.hasNext()) {
            val job = jobIterator.next()
            if (job.clientId == clientId) {
                validations.add(job)
                jobIterator.remove()
            }
        }
        return validations.toList()
    }

    private fun validate() {
        val result = validation.validateAll(pending)
        pending.clear()
        result.first.forEach { validationResults.add(it) }
        result.second.forEach { waitingForScheduling.add(it) }
    }

    private fun plan() {
        val result = planning.scheduleAll(waitingForScheduling)
        waitingForScheduling.clear()
        result.forEach { scheduled.add(it) }
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