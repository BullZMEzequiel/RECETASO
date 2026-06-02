package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.R
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.databinding.ActivityRecetaDetalleBinding
import com.example.recetarioboliviano.modelo.entidades.PasoPreparacion
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.modelo.entidades.RecetaIngrediente
import com.example.recetarioboliviano.modelo.util.ImageHelper
import com.example.recetarioboliviano.vista.adaptadores.PasoDetalleAdapter
import com.example.recetarioboliviano.vistamodelo.RecetaViewModel

/**
 * Activity para mostrar los detalles de una receta.
 */
class RecetaDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecetaDetalleBinding
    private val viewModel: RecetaViewModel by viewModels()
    private lateinit var pasoAdapter: PasoDetalleAdapter

    private var recetaActual: Receta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecetaDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recetaId = intent.getStringExtra("receta_id")
        if (recetaId != null) {
            setupRecyclerView()
            cargarReceta(recetaId)
        } else {
            Toast.makeText(this, "Error al cargar receta", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        pasoAdapter = PasoDetalleAdapter()
        binding.rvPasos.layoutManager = LinearLayoutManager(this)
        binding.rvPasos.adapter = pasoAdapter
    }

    private fun setupClickListeners() {
        binding.btnFavorito.setOnClickListener {
            onFavoritoClick()
        }
    }

    private fun cargarReceta(recetaId: String) {
        viewModel.obtenerRecetaPorId(recetaId).observe(this) { receta ->
            receta?.let {
                recetaActual = it
                mostrarReceta(it)
                cargarDetallesAdicionales(recetaId)
            }
        }
    }

    private fun cargarDetallesAdicionales(recetaId: String) {
        viewModel.obtenerIngredientes(recetaId).observe(this) { ingredientes ->
            mostrarIngredientes(ingredientes)
        }

        viewModel.obtenerPasos(recetaId).observe(this) { pasos ->
            if (pasos.isNotEmpty()) {
                binding.rvPasos.visibility = View.VISIBLE
                binding.tvPreparacionLegacy.visibility = View.GONE
                pasoAdapter.submitList(pasos.sortedBy { it.numero })
            } else {
                binding.rvPasos.visibility = View.GONE
                binding.tvPreparacionLegacy.visibility = View.VISIBLE
                binding.tvPreparacionLegacy.text = "Pasos no disponibles"
            }
        }
    }

    private fun mostrarIngredientes(ingredientes: List<RecetaIngrediente>) {
        if (ingredientes.isNotEmpty()) {
            val texto = ingredientes.joinToString("\n") { 
                if (it.cantidad != null) "• ${it.ingrediente} (${it.cantidad})"
                else "• ${it.ingrediente}"
            }
            binding.tvIngredientes.text = texto
        } else {
            binding.tvIngredientes.text = "No hay ingredientes registrados"
        }
    }

    private fun mostrarReceta(receta: Receta) {
        binding.tvTitulo.text = receta.titulo
        binding.tvDepartamento.text = receta.departamento ?: ""
        binding.tvCategoria.text = receta.categoria ?: ""
        binding.tvTiempo.text = "${receta.tiempoPreparacion ?: 0} min"
        binding.tvCantidad.text = "${receta.cantidadPersonas ?: 0} pers."
        
        // Los ingredientes y pasos se cargan en cargarDetallesAdicionales
        binding.tvIngredientes.text = "Cargando ingredientes..."

        // Mostrar badge si es del usuario (PRIVADA y creada por él)
        val userId = (application as RecetarioApp).repository.obtenerSesionActual()?.id
        binding.chipMiReceta.visibility = if (receta.creadoPor == userId) View.VISIBLE else View.GONE

        // Actualizar icono de favorito
        actualizarIconoFavorito(receta.esFavorito)

        // Cargar imagen principal
        ImageHelper.cargarImagen(binding.ivReceta, receta.imagenUri)
    }

    private fun actualizarIconoFavorito(esFavorito: Boolean) {
        val iconRes = if (esFavorito) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        binding.btnFavorito.setImageResource(iconRes)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detalle_receta, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val userId = (application as RecetarioApp).repository.obtenerSesionActual()?.id
        return when (item.itemId) {
            R.id.action_editar -> {
                if (recetaActual?.creadoPor == userId) {
                    val intent = Intent(this, RecetaFormActivity::class.java)
                    intent.putExtra("receta_id", recetaActual!!.id)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Solo puedes editar tus recetas", Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.action_eliminar -> {
                if (recetaActual?.creadoPor == userId) {
                    confirmarEliminar()
                } else {
                    Toast.makeText(this, "Solo puedes eliminar tus recetas", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onFavoritoClick() {
        recetaActual?.let { receta ->
            viewModel.toggleFavorito(receta)
            val mensaje = if (!receta.esFavorito) "Añadido a favoritos" else "Eliminado de favoritos"
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmarEliminar() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Receta")
            .setMessage("¿Está seguro de eliminar esta receta?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarReceta()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarReceta() {
        recetaActual?.let { receta ->
            val userId = (application as RecetarioApp).repository.currentUser?.id ?: ""
            // Asumiendo que tenemos una forma de saber si es admin
            val esAdmin = false // Deberías obtener esto del perfil del usuario actual
            viewModel.eliminarReceta(receta, userId, esAdmin) { success, error ->
                if (success) {
                    Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, error ?: "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
