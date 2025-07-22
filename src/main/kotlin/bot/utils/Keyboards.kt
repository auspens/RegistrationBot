package org.example.bot.utils

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.example.repository.EventRepository

object Keyboards {
    val startKeyboardAdmin = InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(
                text = "Новое событие",
                callbackData = "admin:createEvent"
            ),
            InlineKeyboardButton.CallbackData(
                text = "Все пользователи",
                callbackData = "admin:allUsers"
            ),
            InlineKeyboardButton.CallbackData(
                text = "Все события",
                callbackData = "admin:manageEvents"
            )
        )
    )
    val changeEventMarkup = InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(
                text = "Фото",
                callbackData = "admin:changeEventPhoto"
            ),
            InlineKeyboardButton.CallbackData(
                text = "Описание",
                callbackData = "admin:changeEventDescription"
            ),
            InlineKeyboardButton.CallbackData(
                text = "Число участников",
                callbackData = "admin:changeEventMaxParticipants"
            ),
            InlineKeyboardButton.CallbackData(
                text = "Готово",
                callbackData = "admin:changeEventFinished"
            )
        )
    )
    val allUsersMenu = InlineKeyboardMarkup.create(
        listOf(
            InlineKeyboardButton.CallbackData(
                text = "Список пользователей",
                callbackData = "admin:exportUsers:allUsers"
            ),
            InlineKeyboardButton.CallbackData(
                text = "Рассылка",
                callbackData = "admin:broadcastMessage:allUsers"
            )
        )
    )

    fun createEventMenu(event: String): InlineKeyboardMarkup {
        return InlineKeyboardMarkup.create(
            listOf(
                InlineKeyboardButton.CallbackData(
                    text = "Участники",
                    callbackData = "admin:exportUsers:$event"
                ),
                InlineKeyboardButton.CallbackData(
                    text = "Рассылка",
                    callbackData = "admin:broadcastMessage:$event"
                ),
                InlineKeyboardButton.CallbackData(
                    text = "Редактировать",
                    callbackData = "admin:changeEvent:$event"
                ),
                InlineKeyboardButton.CallbackData(
                    text = "Удалить",
                    callbackData = "admin:deleteEvent:$event"
                )
            )
        )
    }
    fun createEventsList(eventRepository: EventRepository):InlineKeyboardMarkup{
        return InlineKeyboardMarkup.create(
            eventRepository.getAllEvents().map { event ->
                listOf(
                    InlineKeyboardButton.CallbackData(
                        text = event.eventName,
                        callbackData = "admin:event:${event.eventId}"
                    )
                )
            }
        )
    }
}