package org.example

import com.sun.net.httpserver.HttpServer
import org.example.bot.MyBridgeBot
import org.example.bot.handlers.AdminHandler
import org.example.bot.handlers.CommonHandler
import org.example.bot.handlers.UserHandler
import org.example.models.EventUsers
import org.example.models.Events
import org.example.models.Users
import org.example.repository.FileEventRepository
import org.example.repository.FileUserRepository
import org.example.repository.PostgresEventRepository
import org.example.repository.PostgresUserRepository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.net.URL
import java.net.HttpURLConnection


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

fun initDatabase() {
    Database.connect(
        url = System.getenv("DB_URL"),
        driver = "org.postgresql.Driver",
        user = System.getenv("DB_USER"),
        password = System.getenv("DB_PASSWORD")
    )


    transaction {
        SchemaUtils.createMissingTablesAndColumns(Users, Events, EventUsers)
    }
}

fun main() {
    startMinimalHealthServer()
   initDatabase()

    val userRepo = PostgresUserRepository()
    val eventRepo = PostgresEventRepository()
//    val userRepo = FileUserRepository()
//    val eventRepo = FileEventRepository()
    val adminHandler = AdminHandler(userRepo, eventRepo)
    val commonHandler = CommonHandler(userRepo, eventRepo)
    val userHandler = UserHandler(userRepo, eventRepo)

    val bot = MyBridgeBot(
        adminHandler,
        commonHandler,
        userHandler
    ).createBot()

    bot.startPolling()
    startSelfPing()
}

fun startMinimalHealthServer() {
    try {
        val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
        val server = HttpServer.create(InetSocketAddress(port), 0)
        // Health check endpoint
        server.createContext("/health") { exchange ->
            val response = "OK"
            exchange.responseHeaders.set("Content-Type", "text/plain")
            exchange.responseHeaders.set("Cache-Control", "no-cache")
            exchange.sendResponseHeaders(200, response.length.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }

        // Root endpoint
        server.createContext("/") { exchange ->
            val response = "Telegram Bot is running!"
            exchange.responseHeaders.set("Content-Type", "text/plain")
            exchange.sendResponseHeaders(200, response.length.toLong())
            exchange.responseBody.use { it.write(response.toByteArray()) }
        }

        // Use a small thread pool
        server.executor = Executors.newFixedThreadPool(2)
        server.start()

        println("Health check server started on port 8080")
    } catch (e: Exception) {
        println("Failed to start health server: ${e.message}")
        // Continue anyway - the bot can still work
    }
}

private fun startSelfPing() {
    // Option 1: Using ScheduledExecutorService (Java style)
    val scheduler = Executors.newScheduledThreadPool(1)
    scheduler.scheduleAtFixedRate({
        try {
            val url = URL("https://mybridgebot.onrender.com")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            println("Self-ping: HTTP $responseCode")
            connection.disconnect()
        } catch (e: Exception) {
            println("Self-ping failed: ${e.message}")
        }
    }, 0, 10, TimeUnit.MINUTES)
}

