package patterns.simpleComponent

import patterns.simpleComponent.model.Job

class JobScheduling(storage: Storage) {
    private val validation = Validation()
    private val planning = Planning(storage)


    fun validate(job: Job): Boolean {
        return validation.validate(job)
    }

    fun nextJob(): Int? {
        return planning.nextJob()
    }
}


private class Validation {
    fun validate(job: Job): Boolean {
        return job.content.isNotEmpty()
    }
}


private class Planning(val storage: Storage) {
    fun nextJob(): Int? {
        val all = storage.getAll()
        return (if (all.isNotEmpty()) all.first().first else null)
    }
}