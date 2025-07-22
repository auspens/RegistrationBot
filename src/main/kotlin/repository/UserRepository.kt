package org.example.repository

import org.example.data.UserData

interface UserRepository {
    fun saveUser(userData: UserData)
    fun getUserById(id: Long): UserData?
    fun getAllUsers(): List<UserData>
    fun removeUser(userId: Long)
}