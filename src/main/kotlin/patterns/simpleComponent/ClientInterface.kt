package patterns.simpleComponent

import patterns.simpleComponent.model.Job

class ClientInterface(private val storage: Storage, private val jobScheduling: JobScheduling) {
    fun submit(job: Job): Boolean {
        val valid =  jobScheduling.validate(job)
        if (valid) storage.store(job)
        return valid
    }


}