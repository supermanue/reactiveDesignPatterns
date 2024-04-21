package patterns.errorKernel

import patterns.errorKernel.model.Job
import patterns.errorKernel.model.ValidationResult

class ClientInterface(private val storage: Storage, private val jobScheduling: JobScheduling) {
    fun submit(clientId: Int, job: Job): Int {
        val jobId = storage.store(job)
        jobScheduling.accept(clientId, jobId)
        return jobId
    }

    fun getJobValidations(clientId: Int): List<ValidationResult> {
        return jobScheduling.getJobValidations(clientId)
    }

}