package org.example.bot.handlers

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.types.TelegramBotResult

import org.example.data.UserData
import org.example.models.AdminDraft
import org.example.models.MessageData
import org.example.repository.EventRepository
import org.example.repository.UserRepository

fun handleBroadcastMessage(
    bot: Bot,
    draft: AdminDraft.BroadcastDraft,
    eventRepository: EventRepository,
    message: Message,
    chatId: ChatId,
    adminDrafts: MutableMap<Long, AdminDraft>,
    userRepository: UserRepository
) {
    adminDrafts.remove(message.from!!.id)
    val recipients = if (draft.recipients == "allUsers") {
        userRepository.getAllUsers()
    } else {
        val event = eventRepository.getEventById(draft.recipients)
        val registeredIds = event?.registeredUsersId?.toSet() ?: emptySet()
        userRepository.getAllUsers().filter { it.id in registeredIds }
    }
    val successCount = broadcastMessage(
        receiveMessageToBroadcast(message),
        recipients,
        bot
    )
    bot.sendMessage(chatId, "✅ сообщение отправлено $successCount пользователям.")
}

fun receiveMessageToBroadcast(message: Message): MessageData {
    val messageData = MessageData()
    if (message.text != null) {
        messageData.text = message.text!!
            .replace(Regex("([_*\\[\\]()~`>#+\\-=|{}.!])"), "\\\\$1")
    } else if (message.photo?.isNotEmpty() == true) {
        messageData.photoId = message.photo!!.last().fileId
        messageData.caption = message.caption
    } else if (message.video != null) {
        messageData.videoId = message.video!!.fileId
        messageData.caption = message.caption
    }
    return messageData
}

fun broadcastMessage(messageData: MessageData, users: List<UserData>, bot: Bot): Int {
    var successCount = 0
    for (user in users) {
        var success = false
        val targetChat = ChatId.fromId(user.id)
        if (messageData.text != null) {
            val result = bot.sendMessage(targetChat, messageData.text!!, parseMode = ParseMode.MARKDOWN_V2)
            success = result is TelegramBotResult.Success
        } else if (messageData.photoId != null) {
            val (response, exception) = bot.sendPhoto(
                chatId = targetChat,
                photo = TelegramFile.ByFileId(messageData.photoId!!),
                caption = messageData.caption
            )
            success = response?.isSuccessful == true && exception == null
        } else if (messageData.videoId != null) {
            val (response, exception) = bot.sendVideo(
                chatId = targetChat,
                video = TelegramFile.ByFileId(messageData.videoId!!),
                caption = messageData.caption
            )
            success = response?.isSuccessful == true && exception == null
        }
        if (success) {
            successCount++
        }
    }
    return successCount
}
