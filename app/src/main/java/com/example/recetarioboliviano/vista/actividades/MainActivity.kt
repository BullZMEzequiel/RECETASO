package com.example.recetarioboliviano.vista.actividades

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.recetarioboliviano.R
import com.example.recetarioboliviano.databinding.ActivityMainBinding
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.modelo.entidades.UserRole
import com.example.recetarioboliviano.modelo.util.Constantes
import com.example.recetarioboliviano.modelo.util.ImageHelper
import com.example.recetarioboliviano.vista.adaptadores.RecetaAdapter
import com.example.recetarioboliviano.vistamodelo.RecetaViewModel
import com.example.recetarioboliviano.vistamodelo.UsuarioViewModel
import com.google.android.material.tabs.TabLayout

/**
 * Activity principal con navegación por tabs.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val recetaViewModel: RecetaViewModel by viewModels()
    private val usuarioViewModel: UsuarioViewModel by viewModels()

    private lateinit var adaptador: RecetaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupTabs()
        setupRecyclerView()
        setupSearchView()
        setupSpinnerFiltros()
        setupFab()
        setupBottomNavigation()
        setupUserHeader()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Recetario Boliviano"
    }

    private fun setupUserHeader() {
        binding.userHeader.setOnClickListener {
            irAPerfil()
        }

        binding.btnNotificaciones.setOnClickListener {
            mostrarDialogoDepartamentos()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        recetaViewModel.limpiarFiltros()
                        binding.spinnerFiltro.visibility = View.VISIBLE
                    }
                    1 -> {
                        recetaViewModel.mostrarTodas()
                        binding.spinnerFiltro.visibility = View.VISIBLE
                    }
                    2 -> {
                        recetaViewModel.mostrarFavoritos()
                        binding.spinnerFiltro.visibility = View.GONE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        adaptador = RecetaAdapter(
            onItemClick = { receta -> abrirDetalleReceta(receta) },
            onFavoritoClick = { receta -> recetaViewModel.toggleFavorito(receta) }
        )
        binding.rvRecetas.layoutManager = GridLayoutManager(this, 2)
        binding.rvRecetas.adapter = adaptador
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { recetaViewModel.buscarRecetasPorNombreODepartamento(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { recetaViewModel.buscarRecetasPorNombreODepartamento(it) }
                return true
            }
        })
    }

    private fun setupSpinnerFiltros() {
        val opciones = mutableListOf("Todos")
        opciones.addAll(Constantes.CATEGORIAS)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFiltro.adapter = adapter

        binding.spinnerFiltro.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val categoria = if (position == 0) null else opciones[position]
                recetaViewModel.filtrarPorCategoria(categoria)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFab() {
        binding.btnAgregarTop.setOnClickListener {
            startActivity(Intent(this, RecetaFormActivity::class.java))
        }
        
        binding.btnAdmin.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    binding.tabLayout.getTabAt(0)?.select()
                    true
                }
                R.id.nav_departamentos -> {
                    mostrarDialogoDepartamentos()
                    true
                }
                R.id.nav_favoritos -> {
                    binding.tabLayout.getTabAt(2)?.select()
                    true
                }
                R.id.nav_perfil -> {
                    irAPerfil()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeData() {
        recetaViewModel.recetasFiltradas.observe(this) { recetas ->
            adaptador.submitList(recetas)
            actualizarMensajeVacio(recetas.isEmpty())
        }

        usuarioViewModel.usuarioActual.observe(this) { usuario ->
            if (usuario != null) {
                val primerNombre = usuario.nombre.split(" ").firstOrNull() ?: ""
                binding.tvSaludo.text = "¡Hola, $primerNombre!"
                binding.tvNombreUsuario.text = usuario.nombre
                binding.tvUbicacionUsuario.text = "${usuario.departamento}, ${usuario.pais}"
                binding.tvUbicacionUsuario.visibility = View.VISIBLE
                ImageHelper.cargarAvatar(binding.ivAvatar, usuario.avatarUri)
                
                // Mostrar botón de admin si el rol es ADMIN
                binding.btnAdmin.visibility = if (usuario.role == UserRole.ADMIN) View.VISIBLE else View.GONE
                
                // Refrescar recetas una vez tenemos el perfil (y el userId/role)
                recetaViewModel.cargarRecetas()
            } else {
                binding.tvUbicacionUsuario.visibility = View.GONE
                binding.btnAdmin.visibility = View.GONE
            }
        }
    }

    private fun mostrarDialogoDepartamentos() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Seleccionar Departamento")
        builder.setItems(Constantes.DEPARTAMENTOS_BOLIVIA.toTypedArray()) { _, which ->
            val departamento = Constantes.DEPARTAMENTOS_BOLIVIA[which]
            val intent = Intent(this, RecetasDepartamentoActivity::class.java)
            intent.putExtra("departamento", departamento)
            startActivity(intent)
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun abrirDetalleReceta(receta: Receta) {
        val intent = Intent(this, RecetaDetalleActivity::class.java)
        intent.putExtra("receta_id", receta.id)
        startActivity(intent)
    }

    private fun irAPerfil() {
        startActivity(Intent(this, PerfilActivity::class.java))
    }

    private fun actualizarMensajeVacio(estaVacio: Boolean) {
        binding.tvVacio.visibility = if (estaVacio) View.VISIBLE else View.GONE
        binding.rvRecetas.visibility = if (estaVacio) View.GONE else View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        usuarioViewModel.obtenerPerfil()
    }
}
