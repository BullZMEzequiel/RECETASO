package com.example.recetarioboliviano.modelo.entidades

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecetaIngrediente(
    @SerialName("id")
    val id: String? = null,
    @SerialName("receta_id")
    val recetaId: String,
    @SerialName("ingrediente")
    val ingrediente: String,
    @SerialName("cantidad")
    val cantidad: String? = null
)
