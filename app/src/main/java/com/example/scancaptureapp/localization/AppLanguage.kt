package com.example.scancaptureapp.localization

import java.util.Locale

enum class AppLanguage(val code: String) {
    INDONESIAN("id"),
    ENGLISH("en");

    fun toLocale(): Locale = Locale.forLanguageTag(code)

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.find { it.code == code } ?: INDONESIAN

        fun detectSystemLanguage(): AppLanguage {
            val systemLang = Locale.getDefault().language
            return if (systemLang == ENGLISH.code) ENGLISH else INDONESIAN
        }
    }
}
