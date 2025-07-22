package org.example.bot.handlers

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.example.bot.utils.getUserData
import org.example.bot.utils.getUsernameById
import org.example.bot.utils.messageToAdmin
import org.example.repository.EventRepository
import org.example.repository.UserRepository

class UserHandler(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) {
    fun manageUserCallBackQueries(callbackQuery: CallbackQuery, bot: Bot) {
        if (callbackQuery.message == null) return
        val data = callbackQuery.data
        val userId = callbackQuery.from.id
        val chatId = callbackQuery.message?.chat?.id ?: return
        val registeredUserData = userRepository.getUserById(userId)
        val newUserData = getUserData(callbackQuery.from)
        val inlineKeyboardMarkupContactButton = InlineKeyboardMarkup.create(
            listOf(
                InlineKeyboardButton.CallbackData(
                    text = "Связаться с организаторами",
                    callbackData = "user:contactAdmin"
                )
            )
        )
        when {
            data.startsWith("user:register:") -> {
                when (val event = eventRepository
                    .getEventById(data.removePrefix("user:register:"))) {
                    null -> {
                        bot.sendMessage(ChatId.fromId(chatId), "Событие не существует",
                            replyMarkup = inlineKeyboardMarkupContactButton)
                        return
                    }
                    else -> {
                        if (event.maximumUsers <= event.registeredUsersId.size) {
                            bot.sendMessage(
                                ChatId.fromId(chatId), "К сожалению, места закончились," +
                                        "я сообщил организаторам о вашем запросе",
                                replyMarkup = inlineKeyboardMarkupContactButton
                            )
                            messageToAdmin(
                                "Пользователь ${newUserData.username} " +
                                        "хочет зарегистрироваться на ${event.eventId}, " +
                                        "но мест больше нет", bot
                            )
                            return
                        }
                        val inlineKeyboardMarkupSuccess = InlineKeyboardMarkup.create(
                            listOf(
                                InlineKeyboardButton.CallbackData(
                                    text = "Связаться с организаторами",
                                    callbackData = "user:contactAdmin"
                                ),
                                InlineKeyboardButton.CallbackData(
                                    text = "Отменить регистрацию",
                                    callbackData = "user:cancelRegistration:${event.eventId}"
                                )
                            )
                        )
                        eventRepository.addUserToEvent(userId, event.eventId)
                        if (newUserData != registeredUserData)
                            userRepository.saveUser(newUserData)
                        messageToAdmin(
                            "Пользователь ${getUsernameById(userId, userRepository)} " +
                                    "зарегистрировался на ${event.eventId}", bot
                        )
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            "Вы зарегистрированы на событие: ${event.eventName}",
                            replyMarkup = inlineKeyboardMarkupSuccess
                        )
                    }
                }
            }
            data.startsWith("user:cancelRegistration:")->{
                when (val event = eventRepository
                    .getEventById(data.removePrefix("user:cancelRegistration:"))) {
                    null -> {
                        bot.sendMessage(ChatId.fromId(chatId), "Событие не существует",
                            replyMarkup = inlineKeyboardMarkupContactButton)
                        return
                    }
                    else->{
                        if(event.registeredUsersId.contains(userId)) {
                            eventRepository.removeUserFromEvent(userId, event.eventId)
                            if (!eventRepository.isRegisteredToEvent(userId))
                                userRepository.removeUser(userId)
                            bot.sendMessage(
                                ChatId.fromId(chatId), "Регистрация на событие ${event.eventName} отменена",
                                replyMarkup = inlineKeyboardMarkupContactButton
                            )
                            messageToAdmin(
                                "Пользователь @${newUserData.username} " +
                                        "отменил регистрацию на ${event.eventId}", bot
                            )
                            return
                        }
                        bot.sendMessage(
                            ChatId.fromId(chatId), "Вы не зарегистрированы на это мероприятие",
                            replyMarkup = inlineKeyboardMarkupContactButton
                        )
                    }
                }
            }
            data.startsWith("user:contactAdmin")->{
                val adminContact = System.getenv("ADMIN_CONTACT")
                if (newUserData.username != null) {
                    bot.sendMessage(
                        ChatId.fromId(chatId), "Привет!  Это MyBridge, мы скоро ответим \uD83E\uDE75\n" +
                                "\n" +
                                "Если у вас срочный вопрос, пишите $adminContact"
                    )
                    messageToAdmin(
                        "Пользователь @${newUserData.username} хочет " +
                                "связаться с организаторами", bot
                    )
                }
                else{
                    bot.sendMessage(
                        ChatId.fromId(chatId), "Привет!  Это MyBridge \uD83E\uDE75\n\n" +
                                "к сожалению, мы не видим ваш username, но " +
                                "вы можете связаться с нами, написав $adminContact"
                    )
                }
            }
        }
    }
}