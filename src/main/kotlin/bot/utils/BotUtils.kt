package org.example.bot.utils

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.User
import org.example.data.UserData
import org.example.repository.UserRepository

fun generateDeepLink(param: String): String {
    val userName = System.getenv("BOT_USERNAME")
    return "https://t.me/$userName?start=$param"
}

fun String.extractStartParameter(): String? {
    val parts = this.trim().split(" ", limit = 2)
    return if (parts.size > 1) parts[1].trim() else null
}

fun isAdmin(userId: Long): Boolean = userId == System.getenv("BOT_ADMIN_ID").toLong()

fun getUserData(user: User): UserData {
    return UserData(
        id = user.id,
        firstName = user.firstName,
        lastName = user.lastName,
        username = user.username
    )
}

fun messageToAdmin(message: String, bot: Bot) {
    bot.sendMessage(ChatId.fromId(System.getenv("BOT_ADMIN_ID").toLong()), message)
}

fun userDataToString(userData: UserData): String =
    "User id: ${userData.id}\n" +
            "Username: ${userData.username}\n" +
            "User first name: ${userData.firstName}\n" +
            "User last name: ${userData.lastName}"

fun getUsernameById(id: Long, userRepository: UserRepository): String{
    val name = userRepository.getUserById(id)!!.username
    return if (name != null) "@$name"
    else "No user name"
}