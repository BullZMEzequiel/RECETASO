package com.example.recetarioboliviano.modelo.util

/**
 * Constantes utilizadas en la aplicación.
 */
object Constantes {
    // Departamentos de Bolivia
    val DEPARTAMENTOS_BOLIVIA = listOf(
        "La Paz",
        "Cochabamba",
        "Santa Cruz",
        "Oruro",
        "Potosí",
        "Chuquisaca",
        "Tarija",
        "Beni",
        "Pando"
    )

    // Categorías de recetas
    val CATEGORIAS = listOf(
        "Sopa",
        "Segundo",
        "Postre"
    )

    // Preferencias
    val PREFS_NAME = "recetario_prefs"
    val KEY_USUARIO_REGISTRADO = "usuario_registrado"
    val KEY_USUARIO_ID = "usuario_id"

    // Solicitudes de permisos
    val PERMISSION_REQUEST_CODE = 100
    val CAMERA_PERMISSION_REQUEST_CODE = 101

    // Supabase
    const val SUPABASE_URL = "https://ingthlvnvofotsmarsar.supabase.co"
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImluZ3RobHZudm9mb3RzbWFyc2FyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAyODMyMTIsImV4cCI6MjA5NTg1OTIxMn0.eCfFyHPA_ahB_AJTk2Z40WTgEQdwXQf-74Hke2nruPg"
}
