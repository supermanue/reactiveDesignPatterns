package patterns.simpleComponent

import patterns.simpleComponent.model.Job
import java.util.concurrent.atomic.AtomicInteger

class Storage {

    private val storageDataStructure = HashMap<Int, Job>()
    private var lastElement: AtomicInteger = AtomicInteger(0)

    fun store(job: Job): Int {
        val position = lastElement.addAndGet(1)
        storageDataStructure.put(position, job)
        return position
    }

    fun getAll(): List<Pair<Int, Job>> {
        return storageDataStructure.toList()
    }

    fun get(id: Int): Job? {
        return storageDataStructure.remove(id)
    }

}