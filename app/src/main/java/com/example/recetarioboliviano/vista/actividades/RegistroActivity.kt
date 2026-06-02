package com.example.recetarioboliviano.vista.actividades

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.recetarioboliviano.R
import com.example.recetarioboliviano.databinding.ActivityRegistroBinding
import com.example.recetarioboliviano.modelo.entidades.Usuario
import com.example.recetarioboliviano.modelo.util.Constantes
import com.example.recetarioboliviano.modelo.util.ImageHelper
import com.example.recetarioboliviano.vistamodelo.UsuarioViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity para el registro inicial del usuario.
 */
class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private val viewModel: UsuarioViewModel by viewModels()
    private lateinit var prefs: SharedPreferences

    private var uriImagenSeleccionada: Uri? = null
    private var currentPhotoPath: String? = null

    // Permisos
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            tomarFoto()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            seleccionarDeGaleria()
        } else {
            Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                val uri = Uri.fromFile(File(path))
                uriImagenSeleccionada = uri
                ImageHelper.cargarAvatar(binding.ivAvatar, uri.toString())
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
                uriImagenSeleccionada = localUri
                ImageHelper.cargarAvatar(binding.ivAvatar, localUri.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constantes.PREFS_NAME, Context.MODE_PRIVATE)

        setupSpinnerDepartamentos()
        setupClickListeners()
    }

    private fun setupSpinnerDepartamentos() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Constantes.DEPARTAMENTOS_BOLIVIA
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDepartamento.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.ivAvatar.setOnClickListener {
            mostrarDialogoSeleccionarImagen()
        }

        binding.btnTomarFoto.setOnClickListener {
            verificarPermisoCamara()
        }

        binding.btnSeleccionarGaleria.setOnClickListener {
            verificarPermisoAlmacenamiento()
        }

        binding.btnRegistrar.setOnClickListener {
            validarYRegistrar()
        }
    }

    private fun mostrarDialogoSeleccionarImagen() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar Avatar")
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
        val photoFile = File.createTempFile("AVATAR_${timeStamp}_", ".jpg", storageDir)
        
        currentPhotoPath = photoFile.absolutePath
        val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
        takePictureLauncher.launch(photoUri)
    }

    private fun seleccionarDeGaleria() {
        pickImageLauncher.launch("image/*")
    }

    private fun validarYRegistrar() {
        val nombre = binding.etNombre.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val departamento = binding.spinnerDepartamento.selectedItem?.toString() ?: ""

        if (nombre.isEmpty()) {
            binding.tilNombre.error = "El nombre es obligatorio"
            return
        }
        if (email.isEmpty()) {
            binding.tilEmail.error = "El email es obligatorio"
            return
        }
        if (password.length < 6) {
            binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        binding.tilNombre.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Mostrar progreso
        binding.btnRegistrar.isEnabled = false
        binding.btnRegistrar.text = "Registrando..."

        viewModel.registrarUsuario(email, password, nombre, departamento) { success, error ->
            if (success) {
                // Si hay imagen, subirla
                uriImagenSeleccionada?.let { uri ->
                    val userId = (application as com.example.recetarioboliviano.RecetarioApp).repository.obtenerSesionActual()?.id
                    if (userId != null) {
                        subirAvatar(userId, uri)
                    } else {
                        completarRegistro()
                    }
                } ?: completarRegistro()
            } else {
                binding.btnRegistrar.isEnabled = true
                binding.btnRegistrar.text = "Registrar"
                Toast.makeText(this, error ?: "Error al registrar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun subirAvatar(userId: String, uri: Uri) {
        val bytes = contentResolver.openInputStream(uri)?.readBytes()
        if (bytes != null) {
            viewModel.subirAvatar(userId, bytes) { avatarUrl ->
                completarRegistro()
            }
        } else {
            completarRegistro()
        }
    }

    private fun completarRegistro() {
        prefs.edit().putBoolean(Constantes.KEY_USUARIO_REGISTRADO, true).apply()
        Toast.makeText(this, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
        irAMainActivity()
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
