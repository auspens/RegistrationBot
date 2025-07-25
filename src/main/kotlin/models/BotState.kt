package org.example.models

enum class BotState {
    AWAITING_BROADCAST_MESSAGE,
    AWAITING_EVENT_NAME,
    AWAITING_EVENT_PHOTO,
    AWAITING_EVENT_DESCRIPTION,
    AWAITING_MAX_PARTICIPANTS,
    FINISHED,
    BLOCKED
}