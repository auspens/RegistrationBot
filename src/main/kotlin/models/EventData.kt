package org.example.data

import kotlinx.serialization.Serializable

@Serializable
data class EventData(
    val eventId: String,
    val eventName:String = "",
    var eventPictureId: String?,
    var eventDescription: String?,
    val registeredUsersId: MutableList<Long>,
    var maximumUsers: Long = 0,
    var eventLink: String? = null
)
