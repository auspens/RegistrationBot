package org.example.repository

import org.example.data.EventData

interface EventRepository {
    fun addEvent(eventData: EventData)
    fun deleteEvent(eventId: String)
    fun getEventById(eventId: String): EventData?
    fun getAllEvents(): List<EventData>
    fun addUserToEvent(userId: Long, eventName: String)
    fun removeUserFromEvent(userId: Long, eventName: String)
    fun updateEventInfo(
        eventId: String,
        newPictureId: String? = null,
        newMaximumParticipants: Long = -1,
        newDescription: String? = null
    )
    fun isRegisteredToEvent(userId: Long): Boolean
    fun getUserCountInEvent(eventId: String): Long
}