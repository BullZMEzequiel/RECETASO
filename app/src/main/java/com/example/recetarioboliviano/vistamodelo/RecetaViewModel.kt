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

    fun eliminarPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            try {
                repository.eliminarPlaylist(playlist.id)
                cargarPlaylists()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun agregarRecetaAPlaylist(playlistId: String, recetaId: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.agregarRecetaAPlaylist(playlistId, recetaId)
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error al añadir receta a la carpeta")
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
                
                // 3. Filtrar: Admin ve TODO. Usuario ve SISTEMA o sus propias PRIVADAS.
                val usuarioActual = repository.obtenerUsuarioActual()
                val esAdmin = usuarioActual?.role == UserRole.ADMIN

                val listaFiltradaVisibilidad = if (esAdmin) {
                    listaTotal
                } else {
                    listaTotal.filter { receta ->
                        receta.visibilidad == RecetaVisibilidad.SISTEMA || receta.creadoPor == userId
                    }
                }

                // 4. Marcar favoritos
                val favs = if (userId != null) {
                    try { repository.obtenerFavoritos(userId) } catch(e: Exception) { emptyList() }
                } else emptyList()
                val favIds = favs.map { it.id }.toSet()

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
        busquedaActual = ""
        categoriaActual = null
        departamentoActual = null
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
        departamentoActual = null
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
                // En lugar de cargar todo, actualizamos localmente para más rapidez y luego sincronizamos
                todasLasRecetas = todasLasRecetas.map { 
                    if (it.id == receta.id) it.copy(esFavorito = !receta.esFavorito) else it 
                }
                aplicarFiltros()
            } catch (e: Exception) {
                // Si falla en red, recargamos para estar seguros
                cargarRecetas()
            }
        }
    }

    fun obtenerRecetaPorId(id: String): LiveData<Receta?> {
        val recetaLiveData = MutableLiveData<Receta?>()
        viewModelScope.launch {
            try {
                val receta = repository.obtenerRecetaPorId(id)
                val userId = repository.obtenerSesionActual()?.id
                
                if (receta != null && userId != null) {
                    val favs = try { repository.obtenerFavoritos(userId) } catch(e: Exception) { emptyList() }
                    val esFavorita = favs.any { it.id == id }
                    recetaLiveData.value = receta.copy(esFavorito = esFavorita)
                } else {
                    recetaLiveData.value = receta
                }
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
                val pasosProcesados = pasos.map { paso ->
                    val imagenPasoBytes = pasosImagenes[paso.numero]
                    if (imagenPasoBytes != null) {
                        val fileName = "paso_${receta.id}_${paso.numero}_${System.currentTimeMillis()}.jpg"
                        val path = "recetas/pasos/$fileName"
                        val url = repository.subirImagen("recetas", path, imagenPasoBytes)
                        paso.copy(imagenUri = url)
                    } else {
                        paso
                    }
                }

                // 3. Preparar objeto receta
                val recetaParaGuardar = receta.copy(
                    imagenUri = urlImagenFinal,
                    visibilidad = if (esAdmin) RecetaVisibilidad.SISTEMA else RecetaVisibilidad.PRIVADA,
                    creadoPor = repository.obtenerSesionActual()?.id
                )

                // DISTINGUIR ENTRE CREAR Y ACTUALIZAR
                val existe = try {
                    repository.obtenerRecetaPorId(receta.id) != null
                } catch (e: Exception) {
                    false
                }
                
                if (existe) {
                    repository.actualizarRecetaCompleta(recetaParaGuardar, ingredientes, pasosProcesados)
                } else {
                    if (esAdmin) {
                        repository.crearRecetaSistema(recetaParaGuardar, ingredientes, pasosProcesados)
                    } else {
                        repository.crearReceta(recetaParaGuardar, ingredientes, pasosProcesados)
                    }
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
