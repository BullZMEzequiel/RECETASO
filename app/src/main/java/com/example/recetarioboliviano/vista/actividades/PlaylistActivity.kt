package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
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
            onClick = { playlist -> 
                val intent = Intent(this, AdminPlaylistDetalleActivity::class.java)
                intent.putExtra("playlist_id", playlist.id)
                intent.putExtra("playlist_nombre", playlist.nombre)
                intent.putExtra("playlist_descripcion", playlist.descripcion)
                startActivity(intent)
            },
            onEliminar = { playlist -> confirmarEliminacion(playlist) }
        )
        binding.rvPlaylists.layoutManager = LinearLayoutManager(this)
        binding.rvPlaylists.adapter = adaptador
    }

    private fun observeData() {
        viewModel.playlists.observe(this) { playlists ->
            if (playlists.isNullOrEmpty()) {
                binding.tvSinPlaylists.visibility = View.VISIBLE
                adaptador.submitList(emptyList())
            } else {
                binding.tvSinPlaylists.visibility = View.GONE
                adaptador.submitList(playlists)
            }
        }
    }

    private fun cargarPlaylists() {
        viewModel.cargarPlaylists()
    }

    private fun confirmarEliminacion(playlist: Playlist) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Carpeta")
            .setMessage("¿Estás seguro de eliminar '${playlist.nombre}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarPlaylist(playlist)
                Toast.makeText(this, "Carpeta eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
        viewModel.crearPlaylist(nombre)
        Toast.makeText(this, "Colección '$nombre' creada", Toast.LENGTH_SHORT).show()
    }
}
