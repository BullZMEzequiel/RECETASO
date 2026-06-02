package com.example.recetarioboliviano.modelo.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "recetas")
data class Receta(
    @PrimaryKey
    @SerialName("id")
    val id: String,
    @SerialName("titulo")
    val titulo: String,
    @SerialName("imagen_uri")
    val imagenUri: String? = null,
    @SerialName("tiempo_preparacion")
    val tiempoPreparacion: Int? = null,
    @SerialName("cantidad_personas")
    val cantidadPersonas: Int? = null,
    @SerialName("categoria")
    val categoria: String? = null,
    @SerialName("departamento")
    val departamento: String? = null,
    @SerialName("visibilidad")
    val visibilidad: RecetaVisibilidad = RecetaVisibilidad.PRIVADA,
    @SerialName("creado_por")
    val creadoPor: String? = null,
    @SerialName("fecha_creacion")
    val fechaCreacion: String? = null,
    
    // Campo solo para UI
    val esFavorito: Boolean = false
)
