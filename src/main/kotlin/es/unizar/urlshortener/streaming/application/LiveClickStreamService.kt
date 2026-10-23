package es.unizar.urlshortener.streaming.application

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Service
class LiveClickStreamService :
    RegisterLiveClickListener,
    BroadcastLiveClick {
    private val globalListeners = CopyOnWriteArrayList<ClickListener>()
    private val hashListeners = ConcurrentHashMap<String, CopyOnWriteArrayList<ClickListener>>()

    override fun register(
        hash: String?,
        listener: ClickListener,
    ): AutoCloseable {
        if (hash == null) {
            globalListeners.add(listener)
            return AutoCloseable { globalListeners.remove(listener) }
        } else {
            val list = hashListeners.computeIfAbsent(hash) { CopyOnWriteArrayList() }
            list.add(listener)
            return AutoCloseable {
                list.remove(listener)
                if (list.isEmpty()) {
                    hashListeners.remove(hash, list)
                }
            }
        }
    }

    override fun broadcast(
        hash: String,
        timestamp: Long,
    ) {
        for (listener in globalListeners) {
            listener.onClick(hash, timestamp)
        }
        hashListeners[hash]?.let { list ->
            for (listener in list) {
                listener.onClick(hash, timestamp)
            }
        }
    }
}
