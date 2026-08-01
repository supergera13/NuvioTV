package com.nuvio.tv.core.trakt

import com.nuvio.tv.core.tmdb.TmdbMetadataService
import com.nuvio.tv.domain.model.ContentType
import com.nuvio.tv.domain.model.LibraryEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TraktMetadataLocalizationServiceTest {
    @Test
    fun `localizes entries with a tmdb id and keeps entries without one`() = runTest {
        val tmdbMetadataService = mockk<TmdbMetadataService>()
        coEvery {
            tmdbMetadataService.fetchLocalizedTitle(42, ContentType.MOVIE, "he")
        } returns "סרט מקומי"
        val service = TraktMetadataLocalizationService(tmdbMetadataService)
        val localized = service.localizeLibraryEntries(
            entries = listOf(
                entry(id = "tt123", tmdbId = 42, name = "English title"),
                entry(id = "trakt:99", name = "Fallback title")
            ),
            language = "he"
        )

        assertEquals("סרט מקומי", localized[0].name)
        assertEquals("Fallback title", localized[1].name)
    }

    @Test
    fun `does not query tmdb for english interface language`() = runTest {
        val tmdbMetadataService = mockk<TmdbMetadataService>()
        val service = TraktMetadataLocalizationService(tmdbMetadataService)

        val entries = listOf(entry(id = "tmdb:42", tmdbId = 42, name = "English title"))
        assertEquals(entries, service.localizeLibraryEntries(entries, language = "en-US"))
        coVerify(exactly = 0) {
            tmdbMetadataService.fetchLocalizedTitle(any(), any(), any())
        }
    }

    @Test
    fun `normalizes interface locale tags`() {
        assertEquals("he-IL", TraktMetadataLanguage.resolveInterfaceLanguage("he_il"))
        assertEquals("en", TraktMetadataLanguage.resolveInterfaceLanguage("en"))
        assertEquals("fr", TraktMetadataLanguage.resolveInterfaceLanguage("fr"))
    }

    private fun entry(id: String, name: String, tmdbId: Int? = null) = LibraryEntry(
        id = id,
        type = "movie",
        name = name,
        poster = null,
        background = null,
        logo = null,
        description = null,
        releaseInfo = null,
        imdbRating = null,
        genres = emptyList(),
        addonBaseUrl = null,
        tmdbId = tmdbId
    )
}
