package ru.skyderby.wings.app.api

import java.io.Serializable

/*
* User profile
 */

data class CredentialsMessage (
    val id: Long,
    val name: String,
    val photo: PhotoMessage
) : Serializable

data class PhotoMessage (
    val original: String,
    val medium: String,
    val thumb: String
) : Serializable

/*
* next API category name
 */