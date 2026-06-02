package com.example.recetarioboliviano.modelo.entidades

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    @SerialName("id")
    val id: String,
    @SerialName("usuario_id")
    val usuarioId: String,
    @SerialName("nombre_playlist")
    val nombre: String,
    @SerialName("descripcion")
    val descripcion: String? = null,
    @SerialName("fecha_creacion")
    val fechaCreacion: String? = null
)
