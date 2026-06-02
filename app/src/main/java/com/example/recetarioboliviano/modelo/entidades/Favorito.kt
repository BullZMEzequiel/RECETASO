package com.example.recetarioboliviano.modelo.entidades

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Favorito(
    @SerialName("id")
    val id: String? = null,
    @SerialName("usuario_id")
    val usuarioId: String,
    @SerialName("receta_id")
    val recetaId: String,
    @SerialName("fecha_agregado")
    val fechaAgregado: String? = null
)
