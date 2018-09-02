package ru.skyderby.wings.app.api

data class CredentialsMessage (
    val id: Long,
    val name: String,
    val photo: PhotoMessage
)

data class PhotoMessage (
    val original: String,
    val medium: String,
    val thumb: String
)