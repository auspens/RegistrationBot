package org.example.bot.utils

import org.example.data.EventData
import org.example.repository.EventRepository

fun createEventData(
    eventName: String,
    description: String?,
    pictureId: String?,
    maxUsers: Long,
    eventRepository: EventRepository
): EventData {
    val eventId = generateUniqueEventId(eventName) { id ->
        eventRepository.getEventById(id) != null
    }

    return EventData(
        eventId = eventId,
        eventName = eventName,
        eventPictureId = pictureId,
        eventDescription = description,
        maximumUsers = maxUsers,
        registeredUsersId = mutableListOf()
    )
}

fun generateUniqueEventId(
    eventName: String,
    isInUse: (String) -> Boolean
): String {
    val baseId = generateDeepLinkId(eventName)
    var uniqueId = baseId
    var counter = 1

    while (isInUse(uniqueId)) {
        // Reserve space for the counter suffix (e.g., "-999" = 4 bytes)
        val maxBaseLength = 35
        val baseForCounter = if (baseId.length > maxBaseLength) {
            baseId.substring(0, maxBaseLength)
        } else {
            baseId
        }

        uniqueId = "$baseForCounter-$counter"

        // Ensure even with counter, we don't exceed 40 bytes
        if (uniqueId.toByteArray(Charsets.UTF_8).size > 40) {
            val availableLength = 40 - "-$counter".toByteArray(Charsets.UTF_8).size
            uniqueId = truncateToByteLength(baseForCounter, availableLength) + "-$counter"
        }

        counter++
    }

    return uniqueId
}

fun generateDeepLinkId(name: String): String {
    val processed = name.trim()
        .lowercase()
        .transliterateCyrillicToLatin()
        .replace("\\s+".toRegex(), "-")
        // Remove ALL potentially problematic characters, keeping only safe ones
        .replace("[^a-z0-9\\-_]".toRegex(), "")
        .replace("-+".toRegex(), "-")  // Replace multiple consecutive hyphens with single hyphen
        .replace("_+".toRegex(), "_")  // Replace multiple consecutive underscores with single underscore
        .trim('-', '_')  // Remove leading/trailing hyphens and underscores

    // Ensure the result doesn't exceed 40 bytes and isn't empty
    val result = truncateToByteLength(processed, 40)
    return result.ifEmpty { "event" }
}

/**
 * Truncates a string to fit within the specified byte length when encoded as UTF-8
 */
fun truncateToByteLength(input: String, maxBytes: Int): String {
    if (input.toByteArray(Charsets.UTF_8).size <= maxBytes) {
        return input
    }

    var truncated = input
    while (truncated.toByteArray(Charsets.UTF_8).size > maxBytes && truncated.isNotEmpty()) {
        truncated = truncated.dropLast(1)
    }

    // Remove trailing hyphen or underscore if truncation ended with one
    return truncated.trimEnd('-', '_')
}

fun String.transliterateCyrillicToLatin(): String {
    val map = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e",
        'ё' to "e", 'ж' to "zh", 'з' to "z", 'и' to "i", 'й' to "i", 'к' to "k",
        'л' to "l", 'м' to "m", 'н' to "n", 'о' to "o", 'п' to "p", 'р' to "r",
        'с' to "s", 'т' to "t", 'у' to "u", 'ф' to "f", 'х' to "kh", 'ц' to "ts",
        'ч' to "ch", 'ш' to "sh", 'щ' to "shch", 'ъ' to "", 'ы' to "y", 'ь' to "",
        'э' to "e", 'ю' to "yu", 'я' to "ya"
    )
    return this.map { c -> map[c] ?: map[c.lowercaseChar()] ?: c.toString() }
        .joinToString("")
}