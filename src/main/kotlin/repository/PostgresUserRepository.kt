package org.example.repository

import org.example.data.UserData
import org.example.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PostgresUserRepository : UserRepository {

    override fun saveUser(userData: UserData) {
        transaction {
            Users.insertIgnore {
                it[id] = userData.id
                it[firstName] = userData.firstName
                it[lastName] = userData.lastName
                it[username] = userData.username
            }
            Users.update({ Users.id eq userData.id }) {
                it[firstName] = userData.firstName
                it[lastName] = userData.lastName
                it[username] = userData.username
            }
        }
    }

    override fun getUserById(id: Long): UserData? = transaction {
        Users.selectAll().where { Users.id eq id }
            .map {
                UserData(
                    id = it[Users.id],
                    firstName = it[Users.firstName],
                    lastName = it[Users.lastName],
                    username = it[Users.username]
                )
            }.singleOrNull()
    }

    override fun getAllUsers(): List<UserData> = transaction {
        Users.selectAll().map {
            UserData(
                id = it[Users.id],
                firstName = it[Users.firstName],
                lastName = it[Users.lastName],
                username = it[Users.username]
            )
        }
    }

    override fun removeUser(userId: Long): Unit = transaction {
        Users.deleteWhere { id eq userId }
    }
}
