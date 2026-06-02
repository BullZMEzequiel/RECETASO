package com.example.recetarioboliviano.modelo.util

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.recetarioboliviano.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Helper para centralizar la lógica de manejo de imágenes en la aplicación.
 */
object ImageHelper {

    /**
     * Comprime y redimensiona una imagen para optimizar el almacenamiento y la carga.
     * Retorna un ByteArray listo para subir a Supabase.
     */
    fun optimizarImagenParaUpload(context: Context, uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            
            // Redimensionar si es muy grande (ej: máximo 1080px de ancho o alto)
            val maxDimension = 1080
            val width = originalBitmap.width
            val height = originalBitmap.height
            
            val scale = if (width > height) {
                if (width > maxDimension) maxDimension.toFloat() / width else 1f
            } else {
                if (height > maxDimension) maxDimension.toFloat() / height else 1f
            }
            
            val finalBitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    originalBitmap, 
                    (width * scale).toInt(), 
                    (height * scale).toInt(), 
                    true
                )
            } else {
                originalBitmap
            }

            val outputStream = ByteArrayOutputStream()
            // Comprimir a JPEG al 80% de calidad para balance entre peso y nitidez
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Carga una imagen en un ImageView usando Glide con placeholder.
     */
    fun cargarImagen(imageView: ImageView, uriString: String?) {
        val context = imageView.context
        if (uriString.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder)
            return
        }

        val uri = try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            null
        }

        if (uri == null) {
            imageView.setImageResource(R.drawable.ic_image_placeholder)
            return
        }

        // Si es un archivo local, verificar si existe
        if (uri.scheme == "file") {
            val file = File(uri.path ?: "")
            if (!file.exists()) {
                imageView.setImageResource(R.drawable.ic_image_placeholder)
                return
            }
        }

        Glide.with(context)
            .load(uri)
            .centerCrop()
            .placeholder(R.drawable.ic_image_placeholder)
            .error(R.drawable.ic_image_placeholder)
            .into(imageView)
    }

    /**
     * Carga un avatar circular.
     */
    fun cargarAvatar(imageView: ImageView, uriString: String?) {
        val context = imageView.context
        if (uriString.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_avatar_default)
            return
        }

        val uri = try {
            Uri.parse(uriString)
        } catch (e: Exception) {
            null
        }

        Glide.with(context)
            .load(uri)
            .circleCrop()
            .placeholder(R.drawable.ic_avatar_default)
            .error(R.drawable.ic_avatar_default)
            .into(imageView)
    }

    /**
     * Copia una imagen de una URI externa a un archivo local de la aplicación.
     */
    fun copiarImagenAArchivoLocal(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "img_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
