package com.example.recetarioboliviano.modelo.dao

import androidx.room.*
import com.example.recetarioboliviano.modelo.entidades.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * DAO para la entidad Usuario.
 */
@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios LIMIT 1")
    fun obtenerUsuario(): Flow<Usuario?>

    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun obtenerUsuarioSync(): Usuario?

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerUsuarioPorId(id: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario)

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Delete
    suspend fun eliminar(usuario: Usuario)

    @Query("UPDATE usuarios SET avatarUri = :avatarUri WHERE id = :usuarioId")
    suspend fun actualizarAvatar(usuarioId: String, avatarUri: String)
}
