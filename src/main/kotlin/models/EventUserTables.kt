package org.example.models
import org.jetbrains.exposed.sql.Table

object Events : Table("events") {
    val eventId = varchar("event_id", 255) // primary key
    val eventName = varchar("event_name", 255).default("")
    val eventPictureId = varchar("event_picture_id", 255).nullable()
    val eventDescription = text("event_description").nullable()
    val maximumUsers = long("maximum_users").default(0)
    val eventLink = varchar("event_link", 255).nullable()

    override val primaryKey = PrimaryKey(eventId)
}


object EventUsers : Table("event_users") {
    val eventId = reference("event_id", Events.eventId)
    val userId = long("user_id")

    override val primaryKey = PrimaryKey(eventId, userId)
}

object Users : Table("users") {
    val id = long("id").uniqueIndex()         // Telegram user ID
    val firstName = varchar("first_name", 255).nullable()
    val lastName = varchar("last_name", 255).nullable()
    val username = varchar("username", 255).nullable()

    override val primaryKey = PrimaryKey(id)
}

