package com.example.recetarioboliviano.modelo.entidades

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    @SerialName("user") USER,
    @SerialName("admin") ADMIN
}

@Serializable
enum class RecetaVisibilidad {
    @SerialName("sistema") SISTEMA,
    @SerialName("privada") PRIVADA
}
