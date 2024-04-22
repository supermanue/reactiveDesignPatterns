package patterns.letItCrash.model

import kotlinx.serialization.Serializable


@Serializable
data class JobMetadata(val clientId: Int, val jobId: Int, var status: JobStatus)

@Serializable
enum class JobStatus { WAITING, RUNNING, DONE }
