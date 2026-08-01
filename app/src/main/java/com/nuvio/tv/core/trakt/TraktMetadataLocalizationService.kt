package com.nuvio.tv.core.trakt

import com.nuvio.tv.core.tmdb.TmdbMetadataService
import com.nuvio.tv.data.repository.parseContentIds
import com.nuvio.tv.domain.model.ContentType
import com.nuvio.tv.domain.model.LibraryEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktMetadataLocalizationService @Inject constructor(
    private val tmdbMetadataService: TmdbMetadataService
) {
    suspend fun localizeLibraryEntries(
        entries: List<LibraryEntry>,
        language: String = TraktMetadataLanguage.resolveInterfaceLanguage()
    ): List<LibraryEntry> {
        if (entries.isEmpty() || TraktMetadataLanguage.isEnglish(language)) return entries

        return coroutineScope {
            val semaphore = Semaphore(LOCALIZATION_CONCURRENCY)
            entries.map { entry ->
                async {
                    val tmdbId = entry.tmdbId ?: parseContentIds(entry.id).tmdb
                    val contentType = ContentType.fromString(entry.type)
                    if (tmdbId == null || contentType == ContentType.UNKNOWN) return@async entry

                    val localizedTitle = semaphore.withPermit {
                        tmdbMetadataService.fetchLocalizedTitle(
                            tmdbId = tmdbId,
                            contentType = contentType,
                            language = language
                        )
                    } ?: return@async entry

                    if (localizedTitle.equals(entry.name, ignoreCase = true)) {
                        entry
                    } else {
                        entry.copy(name = localizedTitle)
                    }
                }
            }.awaitAll()
        }
    }

    private companion object {
        const val LOCALIZATION_CONCURRENCY = 6
    }
}
