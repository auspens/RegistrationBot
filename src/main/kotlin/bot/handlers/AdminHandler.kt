package org.example.bot.handlers

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.example.bot.utils.Keyboards
import org.example.bot.utils.generateDeepLink
import org.example.bot.utils.isAdmin
import org.example.bot.utils.messageToAdmin


import org.example.models.AdminDraft
import org.example.models.BotState
import org.example.models.EventFields
import org.example.repository.EventRepository
import org.example.repository.UserRepository

class AdminHandler(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) {
   private val adminDrafts = mutableMapOf<Long, AdminDraft>()

    fun setUp(dispatcher: Dispatcher) {
        setUpMessageHandler(dispatcher)
    }

    fun manageAdminCallbackQueries(callbackQuery: CallbackQuery, bot: Bot) {
        if (callbackQuery.message == null) return
        val data = callbackQuery.data
        val userId = callbackQuery.from.id
        if (!isAdmin(userId)) return
        val chatId = callbackQuery.message?.chat?.id ?: return
        when {
            data == "admin:createEvent" -> {
                adminDrafts[userId] = AdminDraft.EventDraft(state = BotState.AWAITING_EVENT_NAME)
                bot.sendMessage(ChatId.fromId(chatId), "Введите название события")
                bot.answerCallbackQuery(callbackQuery.id)
            }

            data == "admin:manageEvents" -> {
                val inlineKeyboardMarkup = Keyboards.createEventsList(eventRepository)
                bot.sendMessage(
                    ChatId.fromId(chatId), "Выберите событие",
                    replyMarkup = inlineKeyboardMarkup
                )
                bot.answerCallbackQuery(callbackQuery.id)
            }

            data.startsWith("admin:event:") -> {
                val event = data.removePrefix("admin:event:")
                val inlineKeyboardMarkup = Keyboards.createEventMenu(event)
                bot.sendMessage(
                    ChatId.fromId(chatId), "Выберите действие для события $event",
                    replyMarkup = inlineKeyboardMarkup
                )
                bot.answerCallbackQuery(callbackQuery.id)
            }

            data.startsWith("admin:changeEvent:") -> {
                adminDrafts[userId] = AdminDraft.ChangesToEvent(
                    state = BotState.BLOCKED,
                    eventId = data.removePrefix("admin:changeEvent:")
                )
                val inlineKeyboardMarkup = Keyboards.changeEventMarkup
                bot.sendMessage(
                    ChatId.fromId(chatId), "Что вы хотите изменить?",
                    replyMarkup = inlineKeyboardMarkup
                )
                bot.answerCallbackQuery(callbackQuery.id)
            }
            data == "admin:changeEventPhoto"->{
                (adminDrafts[userId] as? AdminDraft.ChangesToEvent)?.state = BotState.AWAITING_EVENT_PHOTO
                bot.sendMessage(ChatId.fromId(chatId), "Пришлите новое фото для события. " +
                        "Для выхода отправьте \\start")
            }
            data == "admin:changeEventDescription"->{
                (adminDrafts[userId] as? AdminDraft.ChangesToEvent)?.state = BotState.AWAITING_EVENT_DESCRIPTION
                bot.sendMessage(ChatId.fromId(chatId), "Пришлите описание события. " +
                        "Для выхода отправьте \\start")
            }
            data == "admin:changeEventMaxParticipants" -> {
                (adminDrafts[userId] as? AdminDraft.ChangesToEvent)?.state = BotState.AWAITING_MAX_PARTICIPANTS
                bot.sendMessage(ChatId.fromId(chatId), "Пришлите максимальное число участников. " +
                        "Для выхода отправьте \\start")
            }
            data == "admin:changeEventFinished" ->{
                val draft = adminDrafts[userId]
                (adminDrafts[userId] as? AdminDraft.ChangesToEvent)?.state = BotState.FINISHED
                if (draft is EventFields) {
                    eventRepository.updateEventInfo(
                        draft.eventId!!, draft.eventPhotoFileId,
                        draft.eventMaxParticipants, draft.eventDescription
                    )
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        "✅ Событие успешно изменено. " +
                                "Ссылка для регистрации: ${generateDeepLink(draft.eventId!!)}"
                    )
                }
                adminDrafts.remove(userId)
            }

            data.startsWith("admin:deleteEvent") -> {
                val event = data.removePrefix("admin:deleteEvent:")
                when {
                    eventRepository.getEventById(event) != null -> {
                        eventRepository.deleteEvent(event)
                        bot.sendMessage(ChatId.fromId(chatId), "Событие $event удалено")
                    }
                    else -> bot.sendMessage(ChatId.fromId(chatId), "Событие $event не найдено")
                }
            }

            data == "admin:allUsers" -> {
                val inlineKeyboardMarkup = Keyboards.allUsersMenu
                bot.sendMessage(
                    ChatId.fromId(chatId), "Что вы хотите сделать?",
                    replyMarkup = inlineKeyboardMarkup
                )
                bot.answerCallbackQuery(callbackQuery.id)
            }

            data.startsWith("admin:broadcastMessage") -> {
                adminDrafts[userId] = AdminDraft.BroadcastDraft(
                    state = BotState.AWAITING_BROADCAST_MESSAGE,
                    recipients = data.removePrefix("admin:broadcastMessage:")
                )
                bot.sendMessage(ChatId.fromId(chatId), "Введите сообщение для рассылки")
                bot.answerCallbackQuery(callbackQuery.id)
            }

            data.startsWith("admin:exportUsers") -> {
                val event = data.removePrefix("admin:exportUsers:")
                val message: String
                when {
                    event == "allUsers" -> {
                        message = "Всего пользователей: ${userRepository.getAllUsers().size}\n"
//                                + userRepository.getAllUsers().joinToString(separator = "\n") { user ->
//                            userDataToString(user)
//                        }
                    }

                    eventRepository.getEventById(event) != null -> {
                        val participants = eventRepository.getUserCountInEvent(event)
                        message = if (participants.toInt() != 0) {
                            "Всего зарегистрировано на это событие: $participants\n"
//                            + participants.joinToString(separator = "\n") { user ->
//                                userDataToString(user)
//                            }
                        } else "Нет зарегистрированных участников"
                    }

                    else -> message = "Событие не найдено"
                }
                messageToAdmin(message, bot)
            }
        }
    }

    private fun setUpMessageHandler(
        dispatcher: Dispatcher
    ) {
        dispatcher.message {
            val userId = message.from?.id ?: return@message
            if (!isAdmin(userId)) return@message
            val chatId = ChatId.fromId(userId)
            when (val draft = adminDrafts[userId]) {
                is AdminDraft.EventDraft -> {
                    handleEventCreation(bot, draft, eventRepository, message, chatId, adminDrafts)
                }

                is AdminDraft.BroadcastDraft -> {
                    handleBroadcastMessage(
                        bot, draft, eventRepository, message,
                        chatId, adminDrafts, userRepository
                    )
                }

                is AdminDraft.ChangesToEvent -> {
                    handleAmendmentsToEvent(
                        bot, draft, eventRepository, message,
                        chatId, adminDrafts
                    )
                }

                else -> {}
            }

        }
    }
    fun clearAdminDrafts() = adminDrafts.clear()
}




