package org.example.bot.handlers

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import org.example.bot.utils.Keyboards
import org.example.bot.utils.createEventData

import org.example.bot.utils.generateDeepLink
import org.example.models.AdminDraft
import org.example.models.BotState
import org.example.models.EventFields
import org.example.repository.EventRepository


fun handleAmendmentsToEvent(
    bot: Bot,
    draft: AdminDraft.ChangesToEvent,
    eventRepository: EventRepository,
    message: Message,
    chatId: ChatId,
    adminDrafts: MutableMap<Long, AdminDraft>,
) {
    getEventFieldsEditEvent(bot, draft, message, chatId)
    if (draft.state != BotState.FINISHED)
        return
    eventRepository.updateEventInfo(
        draft.eventId!!, draft.eventPhotoFileId,
        draft.eventMaxParticipants, draft.eventDescription
    )
    bot.sendMessage(
        chatId,
        "✅ Событие успешно изменено. " +
                "Ссылка для регистрации: ${generateDeepLink(draft.eventId!!)}"
    )
    adminDrafts.remove(message.from!!.id)
}

fun handleEventCreation(
    bot: Bot,
    draft: AdminDraft.EventDraft,
    eventRepository: EventRepository,
    message: Message,
    chatId: ChatId,
    adminDrafts: MutableMap<Long, AdminDraft>
) {
    getEventFieldsNewEvent(bot, draft, message, chatId)
    if (draft.state != BotState.FINISHED)
        return
    val newEvent = draft.eventName?.let {
        createEventData(it,
            draft.eventDescription,
            draft.eventPhotoFileId,
            draft.eventMaxParticipants,
            eventRepository)
    }
    if (newEvent != null) {
        eventRepository.addEvent(newEvent)
        bot.sendMessage(
            chatId,
            "✅ Событие успешно создано. " +
                    "Ссылка для регистрации: ${generateDeepLink(newEvent.eventId)}"
        )
    } else bot.sendMessage(
        chatId,
        "Не удалось создать событие"
    )
    adminDrafts.remove(message.from!!.id)
}

fun getEventFieldsNewEvent(
    bot: Bot,
    draft: AdminDraft,
    message: Message,
    chatId: ChatId,
) {
    if (draft !is EventFields)
        return
    when (draft.state) {
        BotState.AWAITING_EVENT_NAME -> {
            expectingEventName(message, draft, bot)
            if (draft.eventName == null)
                return
            draft.state = BotState.AWAITING_EVENT_DESCRIPTION
            bot.sendMessage(
                chatId, "Теперь введите описание события."
            )
        }

        BotState.AWAITING_EVENT_DESCRIPTION -> {
            expectingEventDescription(message, draft)
            draft.state = BotState.AWAITING_EVENT_PHOTO
            bot.sendMessage(chatId, "Теперь отправьте фото события.")
        }

        BotState.AWAITING_EVENT_PHOTO -> {
            expectingEventPhoto(message, draft)
            draft.state = BotState.AWAITING_MAX_PARTICIPANTS
            bot.sendMessage(chatId, "Теперь отправьте максимальное число участников.")
        }

        BotState.AWAITING_MAX_PARTICIPANTS -> {
            expectingEventMaxParticipants(message, draft, bot)
            if (draft.eventMaxParticipants > 0)
                draft.state = BotState.FINISHED
        }

        else -> {}
    }
}

fun getEventFieldsEditEvent(
    bot: Bot,
    draft: AdminDraft,
    message: Message,
    chatId: ChatId,
) {
    if (draft !is EventFields)
        return
    val markup = Keyboards.changeEventMarkup
    when (draft.state) {
        BotState.AWAITING_EVENT_DESCRIPTION -> {
            expectingEventDescription(message, draft)
            draft.state = BotState.BLOCKED
            bot.sendMessage(
                chatId, "Описание события добавлено." +
                        " Нажмите Готово, чтобы сохранить изменения", replyMarkup = markup
            )
        }

        BotState.AWAITING_EVENT_PHOTO -> {
            expectingEventPhoto(message, draft)
            draft.state = BotState.BLOCKED
            bot.sendMessage(
                chatId, "Картинка добавлена." +
                        " Нажмите Готово, чтобы сохранить изменения",
                replyMarkup = markup
            )
        }

        BotState.AWAITING_MAX_PARTICIPANTS -> {
            expectingEventMaxParticipants(message, draft, bot)
            draft.state = BotState.BLOCKED
            bot.sendMessage(
                chatId, "Новое число участников добавлено." +
                        " Нажмите Готово, чтобы сохранить изменения",
                replyMarkup = markup
            )
        }

        else -> {}
    }
}

fun expectingEventName(message: Message, draft: EventFields, bot: Bot) {
    val eventName = message.text
    if (eventName.isNullOrEmpty()) {
        bot.sendMessage(
            ChatId.fromId(message.chat.id), "Поле Название события является обязательным"
        )
        return
    }
    draft.eventName = eventName
}

fun expectingEventMaxParticipants(message: Message, draft: EventFields, bot: Bot) {
    val maxParticipants = message.text?.toLongOrNull()
    if (maxParticipants != null && maxParticipants > 0) {
        draft.eventMaxParticipants = maxParticipants
    } else {
        bot.sendMessage(
            ChatId.fromId(message.chat.id),
            "❗ Пожалуйста, введите число больше нуля."
        )
    }
}

fun expectingEventPhoto(message: Message, draft: EventFields) {
    val photos = message.photo
    if (photos != null) {
        if (photos.isNotEmpty()) {
            val fileId = photos.last().fileId
            draft.eventPhotoFileId = fileId
        }
    }
}

fun expectingEventDescription(message: Message, draft: EventFields) {
    draft.eventDescription = message.text
}