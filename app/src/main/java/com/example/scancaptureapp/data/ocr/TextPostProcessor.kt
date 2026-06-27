package com.example.scancaptureapp.data.ocr

import com.example.scancaptureapp.domain.model.OcrLanguage
import com.example.scancaptureapp.domain.repository.TextFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextPostProcessor @Inject constructor() : TextFormatter {

    override fun format(rawText: String, language: OcrLanguage): String {
        if (rawText.isBlank()) return rawText

        var text = rawText
            .replace('\u00A0', ' ')
            .replace(Regex("[\\u200B-\\u200D\\uFEFF]"), "")

        text = fixLineBreaks(text)
        text = removeNoiseLines(text)
        text = fixSpacing(text)
        text = mergeBrokenWords(text)
        text = applyCommonOcrFixes(text, language)
        text = normalizePunctuation(text)
        text = applySentenceCapitalization(text)

        return text.trim()
    }

    private fun fixLineBreaks(text: String): String {
        val lines = text.lines().map { it.trimEnd() }
        val merged = StringBuilder()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                if (merged.isNotEmpty() && !merged.endsWith("\n\n")) merged.append("\n\n")
                continue
            }
            if (merged.isNotEmpty() && !merged.endsWith("\n") && shouldJoinWithPrevious(merged, trimmed)) {
                merged.append(' ')
            } else if (merged.isNotEmpty() && !merged.endsWith("\n")) {
                merged.append('\n')
            }
            merged.append(trimmed)
        }
        return merged.toString()
    }

    private fun shouldJoinWithPrevious(merged: StringBuilder, nextLine: String): Boolean {
        if (merged.isEmpty()) return false
        val last = merged.last()
        if (last == '-' ) return true
        if (nextLine.first().isLowerCase()) return true
        return merged.length >= 2 && merged[merged.length - 2] == '-' && merged.last() == '\n'
    }

    private fun removeNoiseLines(text: String): String {
        val noisePattern = Regex("""^[\|\[\]\{\}\\\/\|_\-\=\+\*\#\@\$\%\^\&\(\)\.]{3,}$""")
        return text.lines()
            .filterNot { line ->
                val t = line.trim()
                t.length <= 2 && !t.any { it.isLetterOrDigit() } ||
                    noisePattern.matches(t)
            }
            .joinToString("\n")
    }

    private fun fixSpacing(text: String): String {
        return text
            .replace(Regex("""[ \t]{2,}"""), " ")
            .replace(Regex(""" *\n *"""), "\n")
            .replace(Regex("""\n{3,}"""), "\n\n")
    }

    private fun mergeBrokenWords(text: String): String {
        return text
            .replace(Regex("""(\w)-\s+(\w)""")) { m -> "${m.groupValues[1]}${m.groupValues[2]}" }
            .replace(Regex("""(\b\w) \. (\w\b)"""), "$1.$2")
    }

    private fun applyCommonOcrFixes(text: String, language: OcrLanguage): String {
        var result = text

        val wordFixes = listOf(
            Regex("""\bl\s*0\s*l\b""", RegexOption.IGNORE_CASE) to "lol",
            Regex("""\bO(?=\d)""") to "0",
            Regex("""(?<=\d)O\b""") to "0",
            Regex("""\brn\b""") to "m",
            Regex("""\bcl\b""") to "d",
            Regex("""\bvv\b""") to "w",
            Regex("""\|""") to "I"
        )

        for ((pattern, replacement) in wordFixes) {
            result = result.replace(pattern, replacement)
        }
        result = result.replace(Regex("""\b(\d)l\b""")) { "${it.groupValues[1]}1" }

        if (language != OcrLanguage.ENGLISH) {
            result = result
                .replace(Regex("""\bnegara\s+repub1ik\b""", RegexOption.IGNORE_CASE), "negara republik")
                .replace(Regex("""\bRepub1ik\b"""), "Republik")
                .replace(Regex("""\blndonesia\b""", RegexOption.IGNORE_CASE), "Indonesia")
                .replace(Regex("""\bKTP\b"""), "KTP")
        }

        result = result
            .replace(Regex("""(?<=[a-zA-Z])0(?=[a-zA-Z])"""), "O")
            .replace(Regex("""(?<=\s)1(?=[a-z])"""), "I")

        return result
    }

    private fun normalizePunctuation(text: String): String {
        return text
            .replace(Regex("""\s+([,.;:!?])"""), "$1")
            .replace(Regex("""([,.;:!?])([^\s\d])"""), "$1 $2")
            .replace(" ,", ",")
            .replace(" .", ".")
    }

    private fun applySentenceCapitalization(text: String): String {
        val builder = StringBuilder()
        var capitalizeNext = true
        for (ch in text) {
            if (capitalizeNext && ch.isLetter()) {
                builder.append(ch.uppercaseChar())
                capitalizeNext = false
            } else {
                builder.append(ch)
                capitalizeNext = ch == '.' || ch == '!' || ch == '?' || ch == '\n'
            }
        }
        return builder.toString()
    }
}
