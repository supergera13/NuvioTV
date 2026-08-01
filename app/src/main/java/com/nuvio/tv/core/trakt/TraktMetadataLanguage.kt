package com.nuvio.tv.core.trakt

import com.nuvio.tv.LocaleCache
import java.util.Locale

/** Resolves the language used for titles returned by Trakt-backed screens. */
object TraktMetadataLanguage {
    fun resolveInterfaceLanguage(localeTag: String = LocaleCache.localeTag): String {
        val tag = localeTag.trim()
            .takeIf { it.isNotBlank() && it != LocaleCache.UNSET }
            ?: Locale.getDefault().toLanguageTag()
        return normalize(tag)
    }

    fun isEnglish(language: String): Boolean {
        return language.substringBefore('-').equals("en", ignoreCase = true)
    }

    private fun normalize(language: String): String {
        val raw = language.trim()
            .replace('_', '-')
            .takeIf { it.isNotBlank() && !it.equals("und", ignoreCase = true) }
            ?: return "en"
        return raw.split('-')
            .filter { it.isNotBlank() }
            .mapIndexed { index, part ->
                when {
                    index == 0 -> part.lowercase(Locale.US)
                    part.length == 2 -> part.uppercase(Locale.US)
                    else -> part
                }
            }
            .joinToString("-")
            .ifBlank { "en" }
    }
}
