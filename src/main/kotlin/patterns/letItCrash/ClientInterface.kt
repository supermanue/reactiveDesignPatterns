package patterns.letItCrash

import patterns.simpleComponent.model.Job
import patterns.errorKernel.model.ValidationResult

class ClientInterface(private val storage: Storage, private val jobScheduling: JobScheduling) {
    suspend fun submit(clientId: Int, job: Job): Int {
        val jobId = storage.getIdForNewJob()
        jobScheduling.accept(clientId, jobId)
        storage.store(job, jobId)
        return jobId
    }

    suspend fun getJobValidations(clientId: Int): List<ValidationResult> {
        return jobScheduling.getJobValidations(clientId)
    }

}