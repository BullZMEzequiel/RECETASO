package com.example.recetarioboliviano.modelo.entidades

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PasoPreparacion(
    @SerialName("id")
    val id: String? = null,
    @SerialName("receta_id")
    val recetaId: String,
    @SerialName("numero_paso")
    val numero: Int,
    @SerialName("descripcion")
    val descripcion: String,
    @SerialName("imagen_uri")
    val imagenUri: String? = null
)
