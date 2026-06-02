package com.example.recetarioboliviano.modelo.entidades

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistReceta(
    @SerialName("playlist_id")
    val playlistId: String,
    @SerialName("receta_id")
    val recetaId: String,
    @SerialName("fecha_agregado")
    val fechaAgregado: String? = null
)
