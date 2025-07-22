package org.example.models

interface EventFields {
    var eventId: String?
    var eventDescription: String?
    var eventPhotoFileId: String?
    var eventMaxParticipants: Long
    var eventName: String?
}
sealed class AdminDraft {
    abstract var state: BotState

    data class EventDraft(
        override var state: BotState,
        override var eventId: String? = null,
        override var eventName: String? = null,
        override var eventDescription: String? = null,
        override var eventPhotoFileId: String? = null,
        override var eventMaxParticipants: Long = 0
    ) : AdminDraft(), EventFields

    data class BroadcastDraft(
        override var state: BotState,
        var recipients: String,
        var text: String? = null,
        var photoId: String? = null,
        var videoId: String? = null,
        var caption: String? = null
    ) : AdminDraft()

    data class ChangesToEvent(
        override var state: BotState,
        override var eventId: String? = null,
        override var eventDescription: String? = null,
        override var eventPhotoFileId: String? = null,
        override var eventMaxParticipants: Long = 0,
        override var eventName: String? = null
    ) : AdminDraft(), EventFields
}
