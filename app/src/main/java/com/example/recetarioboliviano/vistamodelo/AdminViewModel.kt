package com.example.recetarioboliviano.vistamodelo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.modelo.entidades.Playlist
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.modelo.entidades.Usuario
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RecetarioApp).repository

    private val _usuarios = MutableLiveData<List<Usuario>>()
    val usuarios: LiveData<List<Usuario>> = _usuarios

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _recetasDeUsuario = MutableLiveData<List<Receta>>()
    val recetasDeUsuario: LiveData<List<Receta>> = _recetasDeUsuario

    private val _todasLasRecetas = MutableLiveData<List<Receta>>()
    val todasLasRecetas: LiveData<List<Receta>> = _todasLasRecetas

    private val _recetasDePlaylist = MutableLiveData<List<Receta>>()
    val recetasDePlaylist: LiveData<List<Receta>> = _recetasDePlaylist

    fun cargarTodosLosUsuarios() {
        viewModelScope.launch {
            try {
                _usuarios.value = repository.obtenerTodosLosUsuarios()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun cargarTodasLasRecetas() {
        viewModelScope.launch {
            try {
                _todasLasRecetas.value = repository.obtenerTodasLasRecetas()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun cargarTodasLasPlaylists() {
        viewModelScope.launch {
            try {
                _playlists.value = repository.obtenerTodasLasPlaylists()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun cargarRecetasDeUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                _recetasDeUsuario.value = repository.obtenerRecetasPorUsuario(usuarioId)
            } catch (e: Exception) {
                _recetasDeUsuario.value = emptyList()
            }
        }
    }

    fun cargarRecetasDePlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                _recetasDePlaylist.value = repository.obtenerRecetasPlaylist(playlistId)
            } catch (e: Exception) {
                _recetasDePlaylist.value = emptyList()
            }
        }
    }

    fun cargarPlaylistsDeUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                _playlists.value = repository.obtenerPlaylists(usuarioId)
            } catch (e: Exception) {
                _playlists.value = emptyList()
            }
        }
    }

    fun crearUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                repository.crearUsuario(usuario)
                cargarTodosLosUsuarios()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun actualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            try {
                repository.actualizarUsuario(usuario)
                cargarTodosLosUsuarios()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun eliminarUsuario(usuarioId: String) {
        viewModelScope.launch {
            try {
                repository.eliminarUsuario(usuarioId)
                cargarTodosLosUsuarios()
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    fun eliminarReceta(recetaId: String) {
        viewModelScope.launch {
            try {
                repository.eliminarReceta(recetaId)
                cargarTodasLasRecetas()
                // También recargar si estamos viendo las de un usuario específico
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}
