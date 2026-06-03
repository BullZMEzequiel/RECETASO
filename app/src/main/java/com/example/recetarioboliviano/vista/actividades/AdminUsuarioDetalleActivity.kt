package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.databinding.ActivityAdminUsuarioDetalleBinding
import com.example.recetarioboliviano.vista.adaptadores.PlaylistAdapter
import com.example.recetarioboliviano.vista.adaptadores.RecetaAdapter
import com.example.recetarioboliviano.vistamodelo.AdminViewModel

class AdminUsuarioDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminUsuarioDetalleBinding
    private val viewModel: AdminViewModel by viewModels()
    
    private lateinit var recetaAdapter: RecetaAdapter
    private lateinit var playlistAdapter: PlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminUsuarioDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val usuarioId = intent.getStringExtra("usuario_id")
        val nombreUsuario = intent.getStringExtra("usuario_nombre") ?: "Detalle de Usuario"

        setupToolbar(nombreUsuario)
        setupRecyclerViews()
        observeData()

        if (usuarioId != null) {
            viewModel.cargarRecetasDeUsuario(usuarioId)
            viewModel.cargarPlaylistsDeUsuario(usuarioId)
        }
    }

    private fun setupToolbar(nombre: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = nombre
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerViews() {
        recetaAdapter = RecetaAdapter(
            onItemClick = { receta ->
                val intent = Intent(this, RecetaDetalleActivity::class.java)
                intent.putExtra("receta_id", receta.id)
                startActivity(intent)
            },
            onFavoritoClick = { /* No aplica aquí */ }
        )
        binding.rvRecetasUsuario.layoutManager = LinearLayoutManager(this)
        binding.rvRecetasUsuario.adapter = recetaAdapter

        playlistAdapter = PlaylistAdapter(
            onClick = { playlist ->
                val intent = Intent(this, AdminPlaylistDetalleActivity::class.java)
                intent.putExtra("playlist_id", playlist.id)
                intent.putExtra("playlist_nombre", playlist.nombre)
                intent.putExtra("playlist_descripcion", playlist.descripcion)
                startActivity(intent)
            },
            onEliminar = { playlist ->
                // Admin también podría eliminar playlists si quisiera
            }
        )
        binding.rvPlaylistsUsuario.layoutManager = LinearLayoutManager(this)
        binding.rvPlaylistsUsuario.adapter = playlistAdapter
    }

    private fun observeData() {
        viewModel.recetasDeUsuario.observe(this) { recetas ->
            if (recetas.isNullOrEmpty()) {
                binding.tvSinRecetas.visibility = View.VISIBLE
                recetaAdapter.submitList(emptyList())
            } else {
                binding.tvSinRecetas.visibility = View.GONE
                recetaAdapter.submitList(recetas)
            }
        }

        viewModel.playlists.observe(this) { playlists ->
            if (playlists.isNullOrEmpty()) {
                binding.tvSinPlaylists.visibility = View.VISIBLE
                playlistAdapter.submitList(emptyList())
            } else {
                binding.tvSinPlaylists.visibility = View.GONE
                playlistAdapter.submitList(playlists)
            }
        }
    }
}
