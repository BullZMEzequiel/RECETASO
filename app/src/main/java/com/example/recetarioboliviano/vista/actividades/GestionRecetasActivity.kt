package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.databinding.ActivityGestionRecetasBinding
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.vista.adaptadores.RecetaAdapter
import com.example.recetarioboliviano.vistamodelo.AdminViewModel

class GestionRecetasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionRecetasBinding
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var adaptador: RecetaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGestionRecetasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeData()

        binding.fabAddSystem.setOnClickListener {
            val intent = Intent(this, RecetaFormActivity::class.java)
            // Podríamos pasar un flag para indicar que es receta de sistema
            startActivity(intent)
        }
        
        viewModel.cargarTodasLasRecetas()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestionar Recetas"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        adaptador = RecetaAdapter(
            onItemClick = { receta -> editarReceta(receta) },
            onFavoritoClick = { receta -> confirmarEliminarReceta(receta) }
        )
        binding.rvRecetas.layoutManager = LinearLayoutManager(this)
        binding.rvRecetas.adapter = adaptador
    }

    private fun observeData() {
        viewModel.todasLasRecetas.observe(this) { recetas ->
            if (recetas.isEmpty()) {
                binding.tvVacio.visibility = View.VISIBLE
            } else {
                binding.tvVacio.visibility = View.GONE
                adaptador.submitList(recetas)
            }
        }
    }

    private fun editarReceta(receta: Receta) {
        val intent = Intent(this, RecetaFormActivity::class.java)
        intent.putExtra("receta_id", receta.id)
        startActivity(intent)
    }

    private fun confirmarEliminarReceta(receta: Receta) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Receta")
            .setMessage("¿Estás seguro de eliminar '${receta.titulo}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarReceta(receta.id)
                Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
