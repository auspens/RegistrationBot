package org.example.models

data class MessageData(
    var text: String? = null,
    var photoId: String? = null,
    var videoId: String? = null,
    var caption: String? = null
)
