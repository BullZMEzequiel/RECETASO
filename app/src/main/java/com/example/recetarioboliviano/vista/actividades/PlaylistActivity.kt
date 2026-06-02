package com.example.recetarioboliviano.vista.actividades

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.databinding.ActivityPlaylistBinding
import com.example.recetarioboliviano.modelo.entidades.Playlist
import com.example.recetarioboliviano.vista.adaptadores.PlaylistAdapter
import com.example.recetarioboliviano.vistamodelo.RecetaViewModel
import java.util.UUID

/**
 * Activity para gestionar las colecciones (playlists) de recetas del usuario.
 */
class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private val viewModel: RecetaViewModel by viewModels()
    private lateinit var adaptador: PlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeData()
        
        cargarPlaylists()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mis Colecciones"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        adaptador = PlaylistAdapter(
            onClick = { playlist -> /* Abrir contenido de playlist */ },
            onEliminar = { playlist -> /* Confirmar eliminación */ }
        )
        binding.rvPlaylists.layoutManager = LinearLayoutManager(this)
        binding.rvPlaylists.adapter = adaptador
    }

    private fun observeData() {
        // Necesitamos un LiveData para playlists en el ViewModel
        // viewModel.playlists.observe(this) { ... }
    }

    private fun cargarPlaylists() {
        // Llamar al viewModel
    }

    private fun setupClickListeners() {
        binding.fabAddPlaylist.setOnClickListener {
            mostrarDialogoNuevaPlaylist()
        }
    }

    private fun mostrarDialogoNuevaPlaylist() {
        val input = EditText(this)
        input.hint = "Nombre de la colección"
        
        AlertDialog.Builder(this)
            .setTitle("Nueva Colección")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    crearPlaylist(nombre)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun crearPlaylist(nombre: String) {
        val repository = (application as RecetarioApp).repository
        val userId = repository.obtenerSesionActual()?.id ?: return
        
        val playlist = Playlist(
            id = UUID.randomUUID().toString(),
            nombre = nombre,
            usuarioId = userId
        )
        
        // Aquí llamaríamos al ViewModel para guardar la playlist
        Toast.makeText(this, "Colección '$nombre' creada (Simulado)", Toast.LENGTH_SHORT).show()
    }
}
