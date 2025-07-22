package org.example.data

import kotlinx.serialization.Serializable

    @Serializable
    data class UserData(
        val id: Long,
        val firstName: String?,
        val lastName: String?,
        val username: String?
    )
