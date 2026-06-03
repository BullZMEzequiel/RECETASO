package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.databinding.ActivityAdminPlaylistDetalleBinding
import com.example.recetarioboliviano.vista.adaptadores.RecetaAdapter
import com.example.recetarioboliviano.vistamodelo.AdminViewModel

class AdminPlaylistDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPlaylistDetalleBinding
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adaptador: RecetaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPlaylistDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val playlistId = intent.getStringExtra("playlist_id")
        val nombre = intent.getStringExtra("playlist_nombre") ?: "Detalle de Playlist"
        val descripcion = intent.getStringExtra("playlist_descripcion")

        setupToolbar(nombre)
        binding.tvDescripcion.text = descripcion ?: "Sin descripción"
        
        setupRecyclerView()
        observeData()

        if (playlistId != null) {
            viewModel.cargarRecetasDePlaylist(playlistId)
        }
    }

    private fun setupToolbar(nombre: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = nombre
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        adaptador = RecetaAdapter(
            onItemClick = { receta ->
                val intent = Intent(this, RecetaDetalleActivity::class.java)
                intent.putExtra("receta_id", receta.id)
                startActivity(intent)
            },
            onFavoritoClick = { /* N/A */ }
        )
        binding.rvRecetas.layoutManager = LinearLayoutManager(this)
        binding.rvRecetas.adapter = adaptador
    }

    private fun observeData() {
        viewModel.recetasDePlaylist.observe(this) { recetas ->
            if (recetas.isNullOrEmpty()) {
                binding.tvVacio.visibility = View.VISIBLE
                adaptador.submitList(emptyList())
            } else {
                binding.tvVacio.visibility = View.GONE
                adaptador.submitList(recetas)
            }
        }
    }
}
