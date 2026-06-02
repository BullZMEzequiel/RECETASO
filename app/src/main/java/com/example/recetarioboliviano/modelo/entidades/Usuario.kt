package com.example.recetarioboliviano.modelo.entidades

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Usuario(
    @SerialName("id")
    val id: String,
    @SerialName("nombre")
    val nombre: String,
    @SerialName("departamento")
    val departamento: String? = null,
    @SerialName("pais")
    val pais: String = "Bolivia",
    @SerialName("avatar_uri")
    val avatarUri: String? = null,
    @SerialName("role")
    val role: UserRole = UserRole.USER,
    @SerialName("fecha_registro")
    val fechaRegistro: String? = null
)
