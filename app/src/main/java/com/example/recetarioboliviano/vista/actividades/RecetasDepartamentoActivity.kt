package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.recetarioboliviano.databinding.ActivityRecetasDepartamentoBinding
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.modelo.util.ImageHelper
import com.example.recetarioboliviano.vista.adaptadores.RecetaAdapter
import com.example.recetarioboliviano.vistamodelo.RecetaViewModel
import com.example.recetarioboliviano.vistamodelo.UsuarioViewModel

/**
 * Activity que muestra las recetas de un departamento específico.
 */
class RecetasDepartamentoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecetasDepartamentoBinding
    private val recetaViewModel: RecetaViewModel by viewModels()
    private val usuarioViewModel: UsuarioViewModel by viewModels()

    private lateinit var adaptador: RecetaAdapter
    private lateinit var departamento: String

    companion object {
        const val EXTRA_DEPARTAMENTO = "departamento"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecetasDepartamentoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        departamento = intent.getStringExtra(EXTRA_DEPARTAMENTO) ?: ""

        setupToolbar()
        setupRecyclerView()
        setupFab()
        setupUserHeader()
        cargarRecetas()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = departamento
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.tvDepartamento.text = "Recetas de $departamento"
    }

    private fun setupUserHeader() {
        usuarioViewModel.usuarioActual.observe(this) { usuario ->
            if (usuario != null) {
                binding.tvNombreUsuario.text = usuario.nombre
                binding.tvSaludo.text = "¡Hola!"
                ImageHelper.cargarAvatar(binding.ivAvatar, usuario.avatarUri.orEmpty())
            }
        }
    }

    private fun setupRecyclerView() {
        adaptador = RecetaAdapter(
            onItemClick = { receta -> abrirDetalleReceta(receta) },
            onFavoritoClick = { receta -> recetaViewModel.toggleFavorito(receta) }
        )
        binding.rvRecetas.layoutManager = GridLayoutManager(this, 2)
        binding.rvRecetas.adapter = adaptador
    }

    private fun setupFab() {
        binding.fabAgregar.setOnClickListener {
            val intent = Intent(this, RecetaFormActivity::class.java)
            intent.putExtra("departamento", departamento)
            startActivity(intent)
        }
    }

    private fun cargarRecetas() {
        binding.progressBar.visibility = View.VISIBLE
        recetaViewModel.filtrarPorDepartamento(departamento)
    }

    private fun observeData() {
        recetaViewModel.recetasFiltradas.observe(this) { recetas ->
            binding.progressBar.visibility = View.GONE
            adaptador.submitList(recetas)
            actualizarMensajeVacio(recetas.isEmpty())
        }
    }

    private fun abrirDetalleReceta(receta: Receta) {
        val intent = Intent(this, RecetaDetalleActivity::class.java)
        intent.putExtra("receta_id", receta.id)
        startActivity(intent)
    }

    private fun actualizarMensajeVacio(estaVacio: Boolean) {
        binding.tvVacio.visibility = if (estaVacio) View.VISIBLE else View.GONE
        binding.rvRecetas.visibility = if (estaVacio) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        cargarRecetas()
    }
}
