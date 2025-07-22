package org.example.repository

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.data.EventData
import java.io.File

class FileEventRepository(private val filePath: String = "events.json") :EventRepository{
    @OptIn(ExperimentalSerializationApi::class)
    private val jsonFormat = Json{
        prettyPrint = true
        prettyPrintIndent = "  "
    }
    private val events: MutableMap<String, EventData> = loadEvents().toMutableMap()

    override fun addEvent(eventData: EventData) = synchronized(this) {
        if (events[eventData.eventId] != eventData) {
            events[eventData.eventId] = eventData
            saveEvents()
        }
    }

    override fun getUserCountInEvent(eventId: String): Long =
        events[eventId]!!.registeredUsersId.size.toLong()

    override fun deleteEvent(eventId: String) = synchronized(this){
        events.remove(eventId)
        saveEvents()
    }

    override fun getEventById(eventId: String): EventData? = synchronized(this) {
        events[eventId]
    }

    override fun getAllEvents(): List<EventData>  = synchronized(this){
        events.values.toList()
    }

    override fun addUserToEvent(userId: Long, eventName: String) = synchronized(this) {
        if (events[eventName] == null) {
            println("Event $eventName not found")
            return@synchronized
        }
        if (events[eventName]!!.registeredUsersId.contains(userId))
            return@synchronized
        events[eventName]!!.registeredUsersId.add(userId)
        saveEvents()
    }

    override fun removeUserFromEvent(userId: Long, eventName: String) = synchronized(this) {
        if (events[eventName] == null)
            return@synchronized
        if (events[eventName]!!.registeredUsersId.contains(userId))
            events[eventName]!!.registeredUsersId.remove(userId)
        saveEvents()
    }

    override fun updateEventInfo (
        eventId: String,
        newPictureId: String?,
        newMaximumParticipants: Long,
        newDescription: String?
    ) = synchronized(this){
        val event = events[eventId] ?: return@synchronized
        when {
            newPictureId!=null -> event.eventPictureId = newPictureId
            newMaximumParticipants >= 0 -> event.maximumUsers = newMaximumParticipants
            newDescription != null -> event.eventDescription = newDescription
        }
        saveEvents()
    }

    override fun isRegisteredToEvent(userId: Long): Boolean =
        events.values.any {userId in it.registeredUsersId}


    private fun loadEvents(): Map<String, EventData> {
        val file = File(filePath)
        if (!file.exists()) {
            file.writeText("{}")
            return emptyMap()
        }
        val json = file.readText().trim()
        if (json.isEmpty()) {
            file.writeText("{}")
            return emptyMap()
        }
        return try {
            jsonFormat.decodeFromString<Map<String, EventData>>(json)
        } catch (e: Exception) {
            println("Failed to load events: ${e.message}")
            emptyMap()
        }
    }

    private fun saveEvents(){
        val json = jsonFormat.encodeToString(events)
        File(filePath).writeText(json)
    }
}