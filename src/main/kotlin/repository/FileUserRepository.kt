package org.example.repository

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.data.UserData
import java.io.File

class FileUserRepository(private val filePath: String = "users.json") : UserRepository {
    @OptIn(ExperimentalSerializationApi::class)
    private val jsonFormat = Json{
        prettyPrint = true
        prettyPrintIndent = "  "
    }
    private val users: MutableMap<Long, UserData> = loadUsers().toMutableMap()

    override fun saveUser(userData: UserData) = synchronized(this) {
        if (users[userData.id] != userData) {
            users[userData.id] = userData
            saveUsers()
        }
    }

    override fun getUserById(id: Long): UserData? = synchronized(this) {
        users[id]
    }

    override fun getAllUsers(): List<UserData> = synchronized(this) {
        users.values.toList()
    }

    override fun removeUser(userId: Long) {
        users.remove(userId)
        saveUsers()
    }

    private fun saveUsers() {
        val json = jsonFormat.encodeToString(users)
        File(filePath).writeText(json)
    }

    private fun loadUsers(): Map<Long, UserData> {
        val file = File(filePath)
        if (!file.exists()) {
            file.writeText("{}")
            return emptyMap()
        }
        val json = file.readText().trim()
        if (json.isEmpty()) {
            file.writeText("{}")
            return emptyMap()
        }
        return try {
            jsonFormat.decodeFromString<Map<Long, UserData>>(json)
        } catch (e: Exception) {
            println("Failed to load users: ${e.message}")
            emptyMap()
        }
    }
}