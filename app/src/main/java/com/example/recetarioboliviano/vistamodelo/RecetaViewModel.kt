package com.example.recetarioboliviano.vistamodelo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.modelo.entidades.*
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar las recetas con Supabase.
 */
class RecetaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RecetarioApp).repository

    private val _recetasFiltradas = MutableLiveData<List<Receta>>()
    val recetasFiltradas: LiveData<List<Receta>> = _recetasFiltradas

    private var todasLasRecetas = listOf<Receta>()
    private var busquedaActual = ""
    private var categoriaActual: String? = null
    private var soloFavoritos = false

    private var departamentoActual: String? = null

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    init {
        cargarRecetas()
    }

    fun cargarPlaylists() {
        viewModelScope.launch {
            val userId = repository.obtenerSesionActual()?.id ?: return@launch
            try {
                _playlists.value = repository.obtenerPlaylists(userId)
            } catch (e: Exception) {
                _playlists.value = emptyList()
            }
        }
    }

    fun crearPlaylist(nombre: String, descripcion: String? = null) {
        viewModelScope.launch {
            val userId = repository.obtenerSesionActual()?.id ?: return@launch
            try {
                val nueva = Playlist(
                    id = java.util.UUID.randomUUID().toString(),
                    usuarioId = userId,
                    nombre = nombre,
                    descripcion = descripcion
                )
                repository.crearPlaylist(nueva)
                cargarPlaylists()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun cargarRecetas() {
        viewModelScope.launch {
            try {
                // 1. Obtener todas las recetas de la base de datos
                val listaTotal = repository.obtenerTodasLasRecetas()
                
                // 2. Obtener el ID del usuario actual
                val userId = repository.obtenerSesionActual()?.id
                
                // 3. Filtrar: Mostrar las del sistema (admin) O las creadas por cualquier usuario
                // Con el nuevo esquema, mostramos SISTEMA o PRIVADA creadas por el usuario
                val listaFiltradaVisibilidad = listaTotal.filter { receta ->
                    receta.visibilidad == RecetaVisibilidad.SISTEMA || receta.creadoPor == userId
                }

                // 4. Marcar favoritos
                val favs = if (userId != null) repository.obtenerFavoritos(userId) else emptyList()
                val favIds = favs.map { it.id }.toSet() // El repositorio devuelve una lista de Receta, así que it.id es el ID de la receta favorita

                todasLasRecetas = listaFiltradaVisibilidad.map { it.copy(esFavorito = favIds.contains(it.id)) }
                aplicarFiltros()
            } catch (e: Exception) {
                _recetasFiltradas.value = emptyList()
            }
        }
    }

    fun buscarRecetasPorNombreODepartamento(query: String) {
        busquedaActual = query
        aplicarFiltros()
    }

    fun filtrarPorCategoria(categoria: String?) {
        categoriaActual = if (categoria == "Todos") null else categoria
        aplicarFiltros()
    }

    fun filtrarPorDepartamento(departamento: String?) {
        departamentoActual = departamento
        aplicarFiltros()
    }

    fun mostrarTodas() {
        soloFavoritos = false
        aplicarFiltros()
    }

    fun mostrarFavoritos() {
        soloFavoritos = true
        aplicarFiltros()
    }

    fun limpiarFiltros() {
        busquedaActual = ""
        categoriaActual = null
        soloFavoritos = false
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var filtradas = todasLasRecetas

        if (soloFavoritos) {
            filtradas = filtradas.filter { it.esFavorito }
        }

        if (busquedaActual.isNotEmpty()) {
            filtradas = filtradas.filter {
                it.titulo.contains(busquedaActual, ignoreCase = true) ||
                (it.departamento?.contains(busquedaActual, ignoreCase = true) == true)
            }
        }

        if (categoriaActual != null) {
            filtradas = filtradas.filter { it.categoria == categoriaActual }
        }

        if (departamentoActual != null) {
            filtradas = filtradas.filter { it.departamento == departamentoActual }
        }

        _recetasFiltradas.value = filtradas
    }

    fun toggleFavorito(receta: Receta) {
        viewModelScope.launch {
            val userId = repository.obtenerSesionActual()?.id ?: return@launch
            try {
                repository.toggleFavorito(userId, receta.id, !receta.esFavorito)
                cargarRecetas() // Recargar para actualizar UI
            } catch (e: Exception) {
                // Error
            }
        }
    }

    fun obtenerRecetaPorId(id: String): LiveData<Receta?> {
        val recetaLiveData = MutableLiveData<Receta?>()
        viewModelScope.launch {
            try {
                val receta = repository.obtenerRecetaPorId(id)
                // Obtenemos el ID del usuario actual para marcar favorito
                val userId = repository.obtenerSesionActual()?.id
                val esFavorita = if (userId != null) {
                    val favs = try { repository.obtenerFavoritos(userId) } catch(e: Exception) { emptyList() }
                    favs.any { it.id == id }
                } else false
                
                recetaLiveData.value = receta?.copy(esFavorito = esFavorita)
            } catch (e: Exception) {
                recetaLiveData.value = null
            }
        }
        return recetaLiveData
    }

    fun obtenerIngredientes(recetaId: String): LiveData<List<RecetaIngrediente>> {
        val ingredientesLiveData = MutableLiveData<List<RecetaIngrediente>>()
        viewModelScope.launch {
            try {
                val lista = repository.obtenerIngredientesReceta(recetaId)
                ingredientesLiveData.value = lista
            } catch (e: Exception) {
                ingredientesLiveData.value = emptyList()
            }
        }
        return ingredientesLiveData
    }

    fun obtenerPasos(recetaId: String): LiveData<List<PasoPreparacion>> {
        val pasosLiveData = MutableLiveData<List<PasoPreparacion>>()
        viewModelScope.launch {
            try {
                val lista = repository.obtenerPasosReceta(recetaId)
                pasosLiveData.value = lista
            } catch (e: Exception) {
                pasosLiveData.value = emptyList()
            }
        }
        return pasosLiveData
    }

    fun crearReceta(
        receta: Receta, 
        ingredientes: List<RecetaIngrediente>, 
        pasos: List<PasoPreparacion>, 
        esAdmin: Boolean, 
        imagenBytes: ByteArray? = null,
        pasosImagenes: Map<Int, ByteArray> = emptyMap(),
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                var urlImagenFinal = receta.imagenUri
                
                // 1. Subir imagen principal
                if (imagenBytes != null) {
                    val fileName = "receta_${System.currentTimeMillis()}.jpg"
                    val path = "recetas/$fileName"
                    urlImagenFinal = repository.subirImagen("recetas", path, imagenBytes)
                }

                // 2. Subir imágenes de los pasos
                // Se han eliminado las imágenes de los pasos en el nuevo esquema SQL
                val pasosProcesados = pasos

                // 3. Preparar objeto receta
                val recetaParaGuardar = receta.copy(
                    imagenUri = urlImagenFinal,
                    visibilidad = if (esAdmin) RecetaVisibilidad.SISTEMA else RecetaVisibilidad.PRIVADA,
                    creadoPor = repository.obtenerSesionActual()?.id
                )

                if (esAdmin) {
                    repository.crearRecetaSistema(recetaParaGuardar, ingredientes, pasosProcesados)
                } else {
                    repository.crearReceta(recetaParaGuardar, ingredientes, pasosProcesados)
                }

                cargarRecetas()
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error al crear receta")
            }
        }
    }

    fun eliminarReceta(receta: Receta, usuarioActualId: String, esAdmin: Boolean, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                if (esAdmin || receta.creadoPor == usuarioActualId) {
                    repository.eliminarReceta(receta.id)
                    cargarRecetas()
                    onComplete(true, null)
                } else {
                    onComplete(false, "No tienes permiso para eliminar esta receta")
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error al eliminar")
            }
        }
    }
}
