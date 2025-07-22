package org.example.bot.handlers

import com.github.kotlintelegrambot.dispatcher.Dispatcher
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command

import org.example.bot.utils.isAdmin
import org.example.repository.EventRepository
import org.example.repository.UserRepository

class CommonHandler(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
) {
    fun setUp(
        dispatcher: Dispatcher,
        adminHandler: AdminHandler,
        userHandler: UserHandler
    ) {
        dispatcher.command("start") {
            val userId = message.from?.id ?: return@command
            if (isAdmin(userId)) {
                adminHandler.clearAdminDrafts()
                handleStartCommandAdmin()
            } else
                handleStartCommandUser(eventRepository)
        }
        dispatcher.callbackQuery {
            val data = callbackQuery.data
            when {
                data.startsWith("admin:") -> {
                    if (!isAdmin(callbackQuery.from.id)) return@callbackQuery
                    adminHandler.manageAdminCallbackQueries(callbackQuery, bot)
                }

                data.startsWith("user:") -> {
                    if (isAdmin(callbackQuery.from.id)) return@callbackQuery
                    userHandler.manageUserCallBackQueries(callbackQuery, bot)
                }
            }
        }
    }
}