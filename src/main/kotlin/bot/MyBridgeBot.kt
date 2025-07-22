package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.logging.LogLevel
import org.example.bot.handlers.AdminHandler
import org.example.bot.handlers.CommonHandler
import org.example.bot.handlers.UserHandler
import org.example.repository.EventRepository
import org.example.repository.UserRepository

class MyBridgeBot(
    private val adminHandler: AdminHandler,
    private val commonHandler: CommonHandler,
    private val userHandler: UserHandler
) {

    fun createBot(): Bot {
        return bot {
            timeout = System.getenv("BOT_ANSWER_TIMEOUT").toInt()
            token = System.getenv("BOT_TOKEN")
            logLevel = LogLevel.Error
            dispatch {
                adminHandler.setUp(this)
                commonHandler.setUp(this, adminHandler, userHandler)
            }
        }
    }

}