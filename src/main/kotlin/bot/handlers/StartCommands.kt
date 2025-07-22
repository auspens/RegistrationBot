package org.example.bot.handlers

import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import org.example.bot.utils.Keyboards
import org.example.bot.utils.extractStartParameter
import org.example.repository.EventRepository

fun CommandHandlerEnvironment.handleStartCommandAdmin() {
    val chatId = ChatId.fromId(message.chat.id)
    val inlineKeyboardMarkup = Keyboards.startKeyboardAdmin
    bot.sendMessage(
        chatId, "Вы являетесь админом бота",
        replyMarkup = inlineKeyboardMarkup
    )
}

fun CommandHandlerEnvironment.handleStartCommandUser(
    eventRepository: EventRepository
) {
    val chatId = ChatId.fromId(message.chat.id)
    when (val param = message.text?.extractStartParameter()) {
        null -> {
            bot.sendMessage(
                chatId, "Привет! Это телеграм бот MyBridge. "
            )
        }
        else -> {
            val event = eventRepository.getEventById(param)
            if (event != null) {
                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            text = "Подтвердить регистрацию",
                            callbackData = "user:register:$param"
                        )
                    )
                )
                bot.sendMessage(
                    chatId, "Привет! Это телеграм бот MyBridge. " +
                            "Вы перешли по ссылке для регистрации на мероприятие: ${event.eventName}"
                )
                if (event.eventPictureId != null)
                    bot.sendPhoto(chatId,TelegramFile.ByFileId(event.eventPictureId!!))
                if (event.eventDescription != null)
                    bot.sendMessage(chatId, event.eventDescription!!, replyMarkup = inlineKeyboardMarkup)
                else
                    bot.sendMessage(chatId, "", replyMarkup = inlineKeyboardMarkup)
            }
        }
    }
}