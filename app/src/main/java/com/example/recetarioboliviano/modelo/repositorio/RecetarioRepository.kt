package com.example.recetarioboliviano.modelo.repositorio

import com.example.recetarioboliviano.modelo.entidades.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.auth.providers.builtin.Email
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.minutes

/**
 * Repositorio que proporciona acceso a los datos de la aplicación vía Supabase.
 */
class RecetarioRepository(
    private val supabase: SupabaseClient
) {
    // ================ STORAGE ================
    suspend fun subirImagen(bucket: String, path: String, byteArray: ByteArray): String {
        val bucketRef = supabase.storage.from(bucket)
        bucketRef.upload(path, byteArray) {
            upsert = true
        }
        return bucketRef.publicUrl(path)
    }

    // ================ AUTH ================
    val currentUser get() = supabase.auth.currentUserOrNull()

    fun obtenerSesionActual() = currentUser

    suspend fun signUp(email: String, password: String, nombre: String, departamento: String) {
        try {
            val response = supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = kotlinx.serialization.json.buildJsonObject {
                    put("nombre", kotlinx.serialization.json.JsonPrimitive(nombre))
                    put("departamento", kotlinx.serialization.json.JsonPrimitive(departamento))
                }
            }
            
            // Forzar inserción en tabla pública si tenemos ID, usando postgrest directamente
            val userId = response?.id
            if (userId != null) {
                val usuario = Usuario(
                    id = userId,
                    nombre = nombre,
                    departamento = departamento,
                    role = UserRole.USER
                )
                supabase.postgrest["usuarios"].upsert(usuario)
            }
        } catch (e: Exception) {
            if (e.message?.contains("3 seconds") == true) {
                throw Exception("Por seguridad, espera unos segundos antes de intentar registrarte de nuevo.")
            }
            throw e
        }
    }

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() = supabase.auth.signOut()

    suspend fun eliminarCuentaAutenticacion() {
        // En Supabase, un usuario no puede eliminarse a sí mismo de Auth vía SDK cliente por seguridad 
        // a menos que uses una función RPC o Edge Function. 
        // Por ahora, eliminaremos sus datos de las tablas públicas.
        val userId = supabase.auth.currentUserOrNull()?.id ?: return
        eliminarUsuario(userId)
    }

    // ================ USUARIO ================
    suspend fun obtenerUsuarioActual(): Usuario? {
        val id = supabase.auth.currentUserOrNull()?.id ?: return null
        return supabase.postgrest["usuarios"]
            .select {
                filter {
                    eq("id", id)
                }
            }.decodeSingleOrNull<Usuario>()
    }

    suspend fun actualizarUsuario(usuario: Usuario) {
        supabase.postgrest["usuarios"].upsert(usuario) {
            onConflict = "id"
        }
    }

    suspend fun crearUsuario(usuario: Usuario) {
        supabase.postgrest["usuarios"].insert(usuario)
    }

    suspend fun eliminarUsuario(usuarioId: String) {
        supabase.postgrest["usuarios"].delete {
            filter {
                eq("id", usuarioId)
            }
        }
    }

    // ================ RECETAS ================
    suspend fun obtenerTodasLasRecetas(): List<Receta> {
        return supabase.postgrest["recetas"].select().decodeList<Receta>()
    }

    suspend fun buscarRecetasPorTitulo(query: String): List<Receta> {
        return supabase.postgrest["recetas"].select {
            filter {
                ilike("titulo", "%$query%")
            }
        }.decodeList<Receta>()
    }

    suspend fun obtenerRecetaPorId(id: String): Receta? {
        return supabase.postgrest["recetas"].select {
            filter {
                eq("id", id)
            }
        }.decodeSingleOrNull<Receta>()
    }

    suspend fun obtenerIngredientesReceta(recetaId: String): List<RecetaIngrediente> {
        return supabase.postgrest["receta_ingredientes"].select {
            filter {
                eq("receta_id", recetaId)
            }
        }.decodeList<RecetaIngrediente>()
    }

    suspend fun obtenerPasosReceta(recetaId: String): List<PasoPreparacion> {
        return supabase.postgrest["receta_pasos"].select {
            filter {
                eq("receta_id", recetaId)
            }
        }.decodeList<PasoPreparacion>()
    }

    suspend fun crearReceta(receta: Receta, ingredientes: List<RecetaIngrediente>, pasos: List<PasoPreparacion>) {
        val nuevaReceta = supabase.postgrest["recetas"].insert(receta) {
            select()
        }.decodeSingle<Receta>()

        if (ingredientes.isNotEmpty()) {
            val ingredientesConId = ingredientes.map { it.copy(id = java.util.UUID.randomUUID().toString(), recetaId = nuevaReceta.id) }
            supabase.postgrest["receta_ingredientes"].insert(ingredientesConId)
        }
        if (pasos.isNotEmpty()) {
            val pasosConId = pasos.map { it.copy(id = java.util.UUID.randomUUID().toString(), recetaId = nuevaReceta.id) }
            supabase.postgrest["receta_pasos"].insert(pasosConId)
        }
    }

    suspend fun eliminarReceta(recetaId: String) {
        // Al usar 'on delete cascade' en la base de datos, 
        // borrar la receta padre borrará ingredientes y pasos automáticamente.
        supabase.postgrest["recetas"].delete {
            filter {
                eq("id", recetaId)
            }
        }
    }

    suspend fun actualizarRecetaCompleta(receta: Receta, ingredientes: List<RecetaIngrediente>, pasos: List<PasoPreparacion>) {
        // 1. Actualizar la receta principal
        supabase.postgrest["recetas"].upsert(receta) {
            onConflict = "id"
        }

        // 2. Borrar anteriores
        supabase.postgrest["receta_ingredientes"].delete {
            filter { eq("receta_id", receta.id) }
        }
        supabase.postgrest["receta_pasos"].delete {
            filter { eq("receta_id", receta.id) }
        }

        // 3. Insertar nuevos con IDs únicos generados en la app para evitar colisiones
        if (ingredientes.isNotEmpty()) {
            val ingredientesConId = ingredientes.map { 
                it.copy(id = java.util.UUID.randomUUID().toString(), recetaId = receta.id) 
            }
            supabase.postgrest["receta_ingredientes"].insert(ingredientesConId)
        }
        if (pasos.isNotEmpty()) {
            val pasosConId = pasos.map { 
                it.copy(id = java.util.UUID.randomUUID().toString(), recetaId = receta.id) 
            }
            supabase.postgrest["receta_pasos"].insert(pasosConId)
        }
    }

    // ================ FAVORITOS ================
    suspend fun obtenerFavoritos(usuarioId: String): List<Receta> {
        val favs = supabase.postgrest["favoritos"].select(Columns.raw("receta_id")) {
            filter {
                eq("usuario_id", usuarioId)
            }
        }.decodeList<Favorito>()
        
        if (favs.isEmpty()) return emptyList()
        
        return supabase.postgrest["recetas"].select {
            filter {
                isIn("id", favs.map { it.recetaId })
            }
        }.decodeList<Receta>()
    }

    suspend fun toggleFavorito(usuarioId: String, recetaId: String, esFavorito: Boolean) {
        if (esFavorito) {
            val nuevoFavorito = Favorito(usuarioId = usuarioId, recetaId = recetaId)
            supabase.postgrest["favoritos"].insert(nuevoFavorito)
        } else {
            supabase.postgrest["favoritos"].delete {
                filter {
                    eq("usuario_id", usuarioId)
                    eq("receta_id", recetaId)
                }
            }
        }
    }

    // ================ PLAYLISTS ================
    suspend fun obtenerPlaylists(usuarioId: String): List<Playlist> {
        return supabase.postgrest["playlists"].select {
            filter {
                eq("usuario_id", usuarioId)
            }
        }.decodeList<Playlist>()
    }

    suspend fun obtenerRecetasPlaylist(playlistId: String): List<Receta> {
        val rels = supabase.postgrest["playlist_recetas"].select(Columns.raw("receta_id")) {
            filter {
                eq("playlist_id", playlistId)
            }
        }.decodeList<PlaylistReceta>()

        if (rels.isEmpty()) return emptyList()

        return supabase.postgrest["recetas"].select {
            filter {
                isIn("id", rels.map { it.recetaId })
            }
        }.decodeList<Receta>()
    }

    suspend fun crearPlaylist(playlist: Playlist) {
        supabase.postgrest["playlists"].insert(playlist)
    }

    suspend fun eliminarPlaylist(playlistId: String) {
        supabase.postgrest["playlists"].delete {
            filter {
                eq("id", playlistId)
            }
        }
    }

    suspend fun agregarRecetaAPlaylist(playlistId: String, recetaId: String) {
        supabase.postgrest["playlist_recetas"].insert(PlaylistReceta(playlistId, recetaId))
    }

    // ================ ADMIN FUNCTIONS ================

    suspend fun obtenerTodosLosUsuarios(): List<Usuario> {
        return supabase.postgrest["usuarios"].select().decodeList<Usuario>()
    }

    suspend fun obtenerTodasLasPlaylists(): List<Playlist> {
        return supabase.postgrest["playlists"].select().decodeList<Playlist>()
    }

    suspend fun obtenerRecetasPorUsuario(usuarioId: String): List<Receta> {
        return supabase.postgrest["recetas"].select {
            filter {
                eq("creado_por", usuarioId)
            }
        }.decodeList<Receta>()
    }

    suspend fun obtenerRecetasSistema(): List<Receta> {
        return supabase.postgrest["recetas"].select {
            filter {
                eq("visibilidad", RecetaVisibilidad.SISTEMA)
            }
        }.decodeList<Receta>()
    }

    suspend fun crearRecetaSistema(receta: Receta, ingredientes: List<RecetaIngrediente>, pasos: List<PasoPreparacion>) {
        crearReceta(receta, ingredientes, pasos)
    }
}
