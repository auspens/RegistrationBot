package org.example.repository

import org.example.data.EventData
import org.example.models.EventUsers
import org.example.models.Events
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresEventRepository : EventRepository {

    override fun addEvent(eventData: EventData) {
        transaction {
            Events.insertIgnore { row ->
                row[eventId] = eventData.eventId
                row[eventName] = eventData.eventName
                row[eventPictureId] = eventData.eventPictureId
                row[eventDescription] = eventData.eventDescription
                row[maximumUsers] = eventData.maximumUsers
                row[eventLink] = eventData.eventLink
            }

            eventData.registeredUsersId.forEach { userId ->
                EventUsers.insertIgnore { row ->
                    row[eventId] = eventData.eventId
                    row[EventUsers.userId] = userId
                }
            }
        }
    }

    override fun deleteEvent(eventId: String) {
        transaction {
            EventUsers.deleteWhere { EventUsers.eventId eq eventId }
            Events.deleteWhere { Events.eventId eq eventId }
        }
    }

    override fun getEventById(eventId: String): EventData? {
        return transaction {
            val eventRow = Events
                .selectAll().where { Events.eventId eq eventId }
                .limit(1)
                .singleOrNull()

            eventRow?.let { row ->
                val users = EventUsers
                    .selectAll().where { EventUsers.eventId eq eventId }
                    .map { it[EventUsers.userId] }
                    .toMutableList()

                EventData(
                    eventId = row[Events.eventId],
                    eventName = row[Events.eventName],
                    eventPictureId = row[Events.eventPictureId],
                    eventDescription = row[Events.eventDescription],
                    registeredUsersId = users,
                    maximumUsers = row[Events.maximumUsers],
                    eventLink = row[Events.eventLink]
                )
            }
        }
    }

    override fun getAllEvents(): List<EventData> {
        return transaction {
            Events
                .selectAll()
                .map { row ->
                    val users = EventUsers
                        .selectAll().where { EventUsers.eventId eq row[Events.eventId] }
                        .map { it[EventUsers.userId] }
                        .toMutableList()

                    EventData(
                        eventId = row[Events.eventId],
                        eventName = row[Events.eventName],
                        eventPictureId = row[Events.eventPictureId],
                        eventDescription = row[Events.eventDescription],
                        registeredUsersId = users,
                        maximumUsers = row[Events.maximumUsers],
                        eventLink = row[Events.eventLink]
                    )
                }
        }
    }

    override fun addUserToEvent(userId: Long, eventName: String) {
        transaction {
            val eventId = Events
                .selectAll().where { Events.eventId eq eventName }
                .singleOrNull()?.get(Events.eventId)

            if (eventId != null) {
                EventUsers.insertIgnore {
                    it[EventUsers.eventId] = eventId
                    it[EventUsers.userId] = userId
                }
            } else {
                println("Event $eventName not found")
            }
        }
    }

    override fun removeUserFromEvent(userId: Long, eventName: String) {
        transaction {
            val eventId = Events
                .selectAll().where { Events.eventName eq eventName }
                .singleOrNull()?.get(Events.eventId)

            if (eventId != null) {
                EventUsers.deleteWhere {
                    (EventUsers.eventId eq eventId) and
                            (EventUsers.userId eq userId)
                }
            }
        }
    }

    override fun updateEventInfo(
        eventId: String,
        newPictureId: String?,
        newMaximumParticipants: Long,
        newDescription: String?
    ) {
        transaction {
            Events.update({ Events.eventId eq eventId }) { row ->
                if (newPictureId != null) row[eventPictureId] = newPictureId
                if (newMaximumParticipants >= 0) row[maximumUsers] = newMaximumParticipants
                if (newDescription != null) row[eventDescription] = newDescription
            }
        }
    }

    override fun isRegisteredToEvent(userId: Long): Boolean {
        return transaction {
            EventUsers
                .selectAll().where { EventUsers.userId eq userId }
                .any()
        }
    }

    override fun getUserCountInEvent(eventId: String): Long {
        return transaction {
            EventUsers.selectAll().where { EventUsers.eventId eq eventId }.count()
        }
    }
}
