package es.unizar.urlshortener.streaming.application

import org.jmolecules.architecture.hexagonal.PrimaryPort

fun interface ClickListener {
    fun onClick(
        hash: String,
        timestamp: Long,
    )
}

@PrimaryPort
interface RegisterLiveClickListener {
    fun register(
        hash: String?,
        listener: ClickListener,
    ): AutoCloseable
}

@PrimaryPort
interface BroadcastLiveClick {
    fun broadcast(
        hash: String,
        timestamp: Long,
    )
}
