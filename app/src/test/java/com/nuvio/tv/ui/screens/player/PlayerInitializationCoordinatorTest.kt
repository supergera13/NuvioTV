package com.nuvio.tv.ui.screens.player

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerInitializationCoordinatorTest {

    @Test
    fun `new playback initialization cancels the previous one`() = runTest {
        val coordinator = PlayerInitializationCoordinator(this)
        val firstStarted = CompletableDeferred<Unit>()
        val firstCancelled = CompletableDeferred<Unit>()

        val firstJob = coordinator.launchLatest {
            firstStarted.complete(Unit)
            try {
                awaitCancellation()
            } finally {
                firstCancelled.complete(Unit)
            }
        }
        firstStarted.await()

        var secondInitializationRan = false
        val secondJob = coordinator.launchLatest {
            secondInitializationRan = true
        }

        secondJob.join()
        firstCancelled.await()

        assertFalse(firstJob.isActive)
        assertTrue(secondInitializationRan)
    }
}
