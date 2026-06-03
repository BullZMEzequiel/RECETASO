package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.databinding.ActivityAdminBinding
import com.example.recetarioboliviano.databinding.DialogUsuarioBinding
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
            if (usuarios.isNullOrEmpty()) {
                binding.tvVacio.visibility = View.VISIBLE
            } else {
                binding.tvVacio.visibility = View.GONE
                adaptador.submitList(usuarios)
            }
        }
    }

    private fun mostrarDetallesUsuario(usuario: Usuario) {
        val isOtherAdmin = usuario.role == com.example.recetarioboliviano.modelo.entidades.UserRole.ADMIN
        
        val options = if (isOtherAdmin) {
            arrayOf("Ver Perfil y Recetas (Solo lectura)")
        } else {
            arrayOf("Ver Perfil y Recetas", "Editar Usuario")
        }

        AlertDialog.Builder(this)
            .setTitle(usuario.nombre)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, AdminUsuarioDetalleActivity::class.java)
                        intent.putExtra("usuario_id", usuario.id)
                        intent.putExtra("usuario_nombre", usuario.nombre)
                        startActivity(intent)
                    }
                    1 -> {
                        if (!isOtherAdmin) mostrarDialogoUsuario(usuario)
                    }
                }
            }
            .show()
    }

    private fun mostrarDialogoUsuario(usuario: Usuario? = null) {
        val dialogBinding = DialogUsuarioBinding.inflate(layoutInflater)
        val isEdit = usuario != null
        
        if (isEdit) {
            dialogBinding.etNombre.setText(usuario?.nombre)
            dialogBinding.etDepartamento.setText(usuario?.departamento)
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEdit) "Editar Usuario" else "Nuevo Usuario")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = dialogBinding.etNombre.text.toString()
                val depto = dialogBinding.etDepartamento.text.toString()
                
                if (isEdit) {
                    val actualizado = usuario!!.copy(nombre = nombre, departamento = depto)
                    viewModel.actualizarUsuario(actualizado)
                } else {
                    val nuevo = Usuario(
                        id = java.util.UUID.randomUUID().toString(),
                        nombre = nombre,
                        departamento = depto
                    )
                    viewModel.crearUsuario(nuevo)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarEliminarUsuario(usuario: Usuario) {
        if (usuario.role == com.example.recetarioboliviano.modelo.entidades.UserRole.ADMIN) {
            Toast.makeText(this, "No puedes eliminar a otro administrador", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
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
        
        binding.fabAddUser.setOnClickListener {
            mostrarDialogoUsuario()
        }
    }
}
