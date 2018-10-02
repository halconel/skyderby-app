package ru.skyderby.wings.app.api

/*
* User profile
 */

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

/*
* API category name
 */