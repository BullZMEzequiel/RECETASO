package com.example.recetarioboliviano.vistamodelo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.modelo.entidades.Usuario
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar la información del usuario con Supabase.
 */
class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as RecetarioApp).repository

    private val _usuarioActual = MutableLiveData<Usuario?>()
    val usuarioActual: LiveData<Usuario?> = _usuarioActual

    init {
        obtenerPerfil()
    }

    fun obtenerPerfil() {
        viewModelScope.launch {
            try {
                _usuarioActual.value = repository.obtenerUsuarioActual()
            } catch (e: Exception) {
                _usuarioActual.value = null
            }
        }
    }

    fun registrarUsuario(email: String, pass: String, nombre: String, departamento: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.signUp(email, pass, nombre, departamento)
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error al registrar usuario")
            }
        }
    }

    fun login(email: String, pass: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.signIn(email, pass)
                obtenerPerfil()
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.signOut()
            _usuarioActual.value = null
        }
    }

    fun actualizarUsuario(
        usuario: Usuario, 
        avatarBytes: ByteArray? = null, 
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                var finalAvatarUri = usuario.avatarUri
                
                // 1. Subir nuevo avatar si existe
                if (avatarBytes != null) {
                    val path = "avatars/${usuario.id}.jpg"
                    finalAvatarUri = repository.subirImagen("usuarios", path, avatarBytes)
                }
                
                // 2. Actualizar perfil con la nueva URI
                val usuarioActualizado = usuario.copy(avatarUri = finalAvatarUri)
                repository.actualizarUsuario(usuarioActualizado)
                
                _usuarioActual.value = usuarioActualizado
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error al actualizar usuario")
            }
        }
    }

    fun eliminarUsuarioActual(onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = repository.obtenerSesionActual()?.id ?: return@launch
                repository.eliminarCuentaAutenticacion()
                logout()
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Error al eliminar cuenta")
            }
        }
    }

    fun subirAvatar(userId: String, bytes: ByteArray, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val path = "avatars/$userId.jpg"
                val publicUrl = repository.subirImagen("usuarios", path, bytes)
                
                val usuario = repository.obtenerUsuarioActual()
                if (usuario != null) {
                    val usuarioActualizado = usuario.copy(avatarUri = publicUrl)
                    repository.actualizarUsuario(usuarioActualizado)
                    _usuarioActual.value = usuarioActualizado
                }
                
                onComplete(publicUrl)
            } catch (e: Exception) {
                onComplete(null)
            }
        }
    }
}
