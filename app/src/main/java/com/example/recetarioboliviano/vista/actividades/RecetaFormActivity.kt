package com.example.recetarioboliviano.vista.actividades

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.recetarioboliviano.R
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.databinding.ActivityRecetaFormBinding
import com.example.recetarioboliviano.modelo.entidades.*
import com.example.recetarioboliviano.modelo.util.Constantes
import com.example.recetarioboliviano.modelo.util.ImageHelper
import com.example.recetarioboliviano.vistamodelo.RecetaViewModel
import com.example.recetarioboliviano.vistamodelo.UsuarioViewModel
import com.example.recetarioboliviano.vista.adaptadores.PasoAdapter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity para crear o editar recetas con Supabase.
 */
class RecetaFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecetaFormBinding
    private val viewModel: RecetaViewModel by viewModels()
    private val usuarioViewModel: UsuarioViewModel by viewModels()

    private var imagenUri: Uri? = null
    private var currentPhotoPath: String? = null
    private var recetaExistente: Receta? = null
    private var esModoEdicion: Boolean = false

    // Pasos de preparación
    private val pasos = mutableListOf<PasoPreparacion>()
    private lateinit var pasoAdapter: PasoAdapter
    private var pasoEditandoImagen: Int = -1

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) tomarFoto() }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) seleccionarDeGaleria() }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                imagenUri = Uri.fromFile(File(path))
                ImageHelper.cargarImagen(binding.ivReceta, imagenUri.toString())
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = ImageHelper.copiarImagenAArchivoLocal(this, it)
            if (localPath != null) {
                val localUri = Uri.fromFile(File(localPath))
                imagenUri = localUri
                ImageHelper.cargarImagen(binding.ivReceta, localUri.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecetaFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recetaId = intent.getStringExtra("receta_id")
        if (recetaId != null) {
            esModoEdicion = true
            cargarRecetaExistente(recetaId)
        }

        setupToolbar()
        setupUserHeader()
        setupSpinners()
        setupPasosRecyclerView()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (esModoEdicion) "Editar Receta" else "Nueva Receta"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
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

    private fun setupSpinners() {
        val adapterDeptos = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constantes.DEPARTAMENTOS_BOLIVIA)
        adapterDeptos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDepartamento.adapter = adapterDeptos

        val adapterCategorias = ArrayAdapter(this, android.R.layout.simple_spinner_item, Constantes.CATEGORIAS)
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapterCategorias

        val unidades = arrayOf("min", "h")
        val adapterUnidades = ArrayAdapter(this, android.R.layout.simple_spinner_item, unidades)
        adapterUnidades.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerUnidadTiempo.adapter = adapterUnidades
    }

    private fun setupPasosRecyclerView() {
        pasoAdapter = PasoAdapter(
            onEliminarPaso = { numero -> eliminarPaso(numero) },
            onAgregarImagen = { numero -> 
                pasoEditandoImagen = numero
                mostrarDialogoSeleccionarImagenPaso() 
            },
            onQuitarImagen = { numero -> quitarImagenPaso(numero) },
            onDescripcionChanged = { numero, desc -> actualizarDescripcionPaso(numero, desc) }
        )
        binding.rvPasos.layoutManager = LinearLayoutManager(this)
        binding.rvPasos.adapter = pasoAdapter
    }

    private fun mostrarDialogoSeleccionarImagenPaso() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")
        AlertDialog.Builder(this)
            .setTitle("Imagen del Paso")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> verificarPermisoCamaraPaso()
                    1 -> verificarPermisoAlmacenamientoPaso()
                }
            }
            .show()
    }

    private fun verificarPermisoCamaraPaso() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            tomarFotoPaso()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun verificarPermisoAlmacenamientoPaso() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            seleccionarDeGaleriaPaso()
        } else {
            storagePermissionLauncher.launch(permission)
        }
    }

    private fun tomarFotoPaso() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        val photoFile = File.createTempFile("PASO_${timeStamp}_", ".jpg", storageDir)
        currentPhotoPath = photoFile.absolutePath
        val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        takePictureLauncherPaso.launch(photoUri)
    }

    private fun seleccionarDeGaleriaPaso() {
        pickImageLauncherPaso.launch("image/*")
    }

    private val takePictureLauncherPaso = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoPath != null) {
            actualizarImagenPaso(Uri.fromFile(File(currentPhotoPath!!)).toString())
        }
    }

    private val pickImageLauncherPaso = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = ImageHelper.copiarImagenAArchivoLocal(this, it)
            if (localPath != null) {
                actualizarImagenPaso(Uri.fromFile(File(localPath)).toString())
            }
        }
    }

    private fun actualizarImagenPaso(uriString: String) {
        val index = pasos.indexOfFirst { it.numero == pasoEditandoImagen }
        if (index != -1) {
            // Nota: El campo imagenUri se eliminó del modelo PasoPreparacion en el nuevo esquema SQL
            // Si necesitas mostrar la imagen temporalmente en la UI, podrías usar una lista paralela 
            // o restaurar el campo en la entidad si decides usarlo (pero el SQL no lo tiene).
            // Por ahora, como el SQL no tiene imagen_uri en receta_pasos, lo comentamos o manejamos aparte.
            // pasos[index] = pasos[index].copy(imagenUri = uriString)
            actualizarListaPasos()
        }
    }

    private fun quitarImagenPaso(numero: Int) {
        val index = pasos.indexOfFirst { it.numero == numero }
        if (index != -1) {
            // pasos[index] = pasos[index].copy(imagenUri = null)
            actualizarListaPasos()
        }
    }

    private fun setupClickListeners() {
        binding.cardImagen.setOnClickListener { mostrarDialogoSeleccionarImagen() }
        binding.btnAgregarPaso.setOnClickListener { agregarPaso() }
        binding.btnGuardar.setOnClickListener { validarYGuardar() }
    }

    private fun cargarRecetaExistente(recetaId: String) {
        viewModel.obtenerRecetaPorId(recetaId).observe(this) { receta ->
            receta?.let {
                recetaExistente = it
                binding.etTitulo.setText(it.titulo)
                binding.etCantidad.setText(it.cantidadPersonas?.toString() ?: "")
                binding.etTiempo.setText(it.tiempoPreparacion?.toString() ?: "")
                
                val deptoPosition = Constantes.DEPARTAMENTOS_BOLIVIA.indexOf(it.departamento)
                if (deptoPosition >= 0) binding.spinnerDepartamento.setSelection(deptoPosition)

                val catPosition = Constantes.CATEGORIAS.indexOf(it.categoria)
                if (catPosition >= 0) binding.spinnerCategoria.setSelection(catPosition)

                ImageHelper.cargarImagen(binding.ivReceta, it.imagenUri.orEmpty())
            }
        }

        viewModel.obtenerIngredientes(recetaId).observe(this) { lista ->
            if (lista.isNotEmpty()) {
                val texto = lista.joinToString("\n") { it.ingrediente }
                binding.etIngredientes.setText(texto)
            }
        }

        viewModel.obtenerPasos(recetaId).observe(this) { lista ->
            if (lista.isNotEmpty()) {
                pasos.clear()
                pasos.addAll(lista.sortedBy { it.numero })
                actualizarListaPasos()
            }
        }
    }

    private fun agregarPaso() {
        val numeroSiguiente = pasos.size + 1
        pasos.add(PasoPreparacion(recetaId = "", numero = numeroSiguiente, descripcion = ""))
        actualizarListaPasos()
    }

    private fun eliminarPaso(numero: Int) {
        pasos.removeIf { it.numero == numero }
        val nuevosPasos = pasos.mapIndexed { index, paso -> paso.copy(numero = index + 1) }
        pasos.clear()
        pasos.addAll(nuevosPasos)
        actualizarListaPasos()
    }

    private fun actualizarDescripcionPaso(numero: Int, descripcion: String) {
        val index = pasos.indexOfFirst { it.numero == numero }
        if (index != -1) {
            pasos[index] = pasos[index].copy(descripcion = descripcion)
        }
    }

    private fun actualizarListaPasos() {
        pasoAdapter.submitList(pasos.toList())
        binding.tvSinPasos.visibility = if (pasos.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun mostrarDialogoSeleccionarImagen() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")
        AlertDialog.Builder(this)
            .setTitle("Imagen de la Receta")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> verificarPermisoCamara()
                    1 -> verificarPermisoAlmacenamiento()
                }
            }
            .show()
    }

    private fun verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            tomarFoto()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun verificarPermisoAlmacenamiento() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            seleccionarDeGaleria()
        } else {
            storagePermissionLauncher.launch(permission)
        }
    }

    private fun tomarFoto() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        val photoFile = File.createTempFile("RECETA_${timeStamp}_", ".jpg", storageDir)
        currentPhotoPath = photoFile.absolutePath
        val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(photoUri)
    }

    private fun seleccionarDeGaleria() { pickImageLauncher.launch("image/*") }

    private fun validarYGuardar() {
        val titulo = binding.etTitulo.text.toString().trim()
        val tiempoNum = binding.etTiempo.text.toString().toIntOrNull()
        val cantidad = binding.etCantidad.text.toString().toIntOrNull()
        val departamento = binding.spinnerDepartamento.selectedItem.toString()
        val categoria = binding.spinnerCategoria.selectedItem.toString()
        val ingredientesRaw = binding.etIngredientes.text.toString().trim()

        if (titulo.isEmpty()) {
            Toast.makeText(this, "El título es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar progreso
        binding.btnGuardar.isEnabled = false
        binding.btnGuardar.text = "Guardando..."

        // Optimizar imagen si se seleccionó una nueva (es una URI local)
        var imagenBytes: ByteArray? = null
        if (imagenUri != null && (imagenUri?.scheme == "file" || imagenUri?.scheme == "content")) {
            imagenBytes = ImageHelper.optimizarImagenParaUpload(this, imagenUri!!)
        }

        // Convertir ingredientes texto a lista de objetos
        val ingredientesList = ingredientesRaw.split("\n").filter { it.isNotBlank() }.map {
            RecetaIngrediente(recetaId = "", ingrediente = it)
        }

        val receta = Receta(
            id = recetaExistente?.id ?: java.util.UUID.randomUUID().toString(),
            titulo = titulo,
            tiempoPreparacion = tiempoNum,
            cantidadPersonas = cantidad,
            categoria = categoria,
            departamento = departamento,
            imagenUri = recetaExistente?.imagenUri
        )

        val esAdmin = usuarioViewModel.usuarioActual.value?.role == UserRole.ADMIN

        // Preparar imágenes de los pasos (Desactivado según nuevo esquema)
        val pasosImagenes = mutableMapOf<Int, ByteArray>()

        viewModel.crearReceta(receta, ingredientesList, pasos, esAdmin, imagenBytes, pasosImagenes) { success, error ->
            binding.btnGuardar.isEnabled = true
            binding.btnGuardar.text = "Guardar Receta"
            
            if (success) {
                Toast.makeText(this, "Receta guardada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, error ?: "Error al guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
