package com.nuvio.tv.ui.screens.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Keeps player construction single-flight: a newer playback request supersedes
 * any initialization that is still resolving metadata, AFR, or decoder setup.
 */
internal class PlayerInitializationCoordinator(
    private val scope: CoroutineScope
) {
    private val lock = Any()
    private var activeJob: Job? = null

    fun launchLatest(block: suspend CoroutineScope.() -> Unit): Job {
        val job = synchronized(lock) {
            activeJob?.cancel()
            scope.launch(start = CoroutineStart.LAZY, block = block).also { newJob ->
                activeJob = newJob
                newJob.invokeOnCompletion {
                    synchronized(lock) {
                        if (activeJob === newJob) {
                            activeJob = null
                        }
                    }
                }
            }
        }
        job.start()
        return job
    }

    fun cancel() {
        synchronized(lock) {
            activeJob?.cancel()
            activeJob = null
        }
    }
}
