package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.databinding.ActivityAdminBinding
import com.example.recetarioboliviano.modelo.entidades.Usuario
import com.example.recetarioboliviano.vista.adaptadores.UsuarioAdapter
import com.example.recetarioboliviano.vistamodelo.AdminViewModel

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val viewModel: AdminViewModel by viewModels()

    private lateinit var adaptador: UsuarioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeData()
        
        viewModel.cargarTodosLosUsuarios()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Panel de Administrador"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupRecyclerView() {
        adaptador = UsuarioAdapter(
            onUsuarioClick = { usuario -> mostrarDetallesUsuario(usuario) },
            onEliminarClick = { usuario -> confirmarEliminarUsuario(usuario) }
        )
        binding.rvUsuarios.layoutManager = LinearLayoutManager(this)
        binding.rvUsuarios.adapter = adaptador
    }

    private fun observeData() {
        viewModel.usuarios.observe(this) { usuarios ->
            if (usuarios.isEmpty()) {
                binding.tvVacio.visibility = View.VISIBLE
            } else {
                binding.tvVacio.visibility = View.GONE
                adaptador.submitList(usuarios)
            }
        }
    }

    private fun mostrarDetallesUsuario(usuario: Usuario) {
        // Podríamos abrir una actividad que muestre las recetas y playlists de este usuario
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Usuario: ${usuario.nombre}")
        builder.setMessage("¿Deseas ver el contenido de este usuario?")
        builder.setPositiveButton("Ver Recetas") { _, _ ->
             // Navegar a una actividad de lista de recetas filtrada por usuario
        }
        builder.setNegativeButton("Cerrar", null)
        builder.show()
    }

    private fun confirmarEliminarUsuario(usuario: Usuario) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de eliminar a ${usuario.nombre}? Esta acción no se puede deshacer y borrará todo su contenido.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarUsuario(usuario.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun setupListeners() {
        binding.btnGestionarRecetas.setOnClickListener {
            val intent = Intent(this, GestionRecetasActivity::class.java)
            startActivity(intent)
        }
    }
}
