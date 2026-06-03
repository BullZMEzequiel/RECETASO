package com.example.recetarioboliviano.vista.adaptadores

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recetarioboliviano.databinding.ItemPasoBinding
import com.example.recetarioboliviano.modelo.entidades.PasoPreparacion
import com.example.recetarioboliviano.modelo.util.ImageHelper

/**
 * Adaptador para mostrar y editar los pasos de preparación de una receta.
 */
class PasoAdapter(
    private val onEliminarPaso: (Int) -> Unit,
    private val onAgregarImagen: (Int) -> Unit,
    private val onQuitarImagen: (Int) -> Unit,
    private val onDescripcionChanged: (Int, String) -> Unit
) : ListAdapter<PasoPreparacion, PasoAdapter.PasoViewHolder>(PasoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasoViewHolder {
        val binding = ItemPasoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PasoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PasoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PasoViewHolder(
        private val binding: ItemPasoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var textWatcher: TextWatcher? = null

        fun bind(paso: PasoPreparacion) {
            // Remover watcher anterior para evitar bucles
            binding.etDescripcionPaso.removeTextChangedListener(textWatcher)
            
            binding.tvNumeroPaso.text = paso.numero.toString()
            binding.etDescripcionPaso.setText(paso.descripcion)

            // Manejo de imagen del paso
            if (!paso.imagenUri.isNullOrEmpty()) {
                binding.ivImagenPaso.visibility = View.VISIBLE
                binding.ivQuitarImagen.visibility = View.VISIBLE
                binding.layoutAgregarImagen.visibility = View.GONE
                ImageHelper.cargarImagen(binding.ivImagenPaso, paso.imagenUri)
            } else {
                binding.ivImagenPaso.visibility = View.GONE
                binding.ivQuitarImagen.visibility = View.GONE
                binding.layoutAgregarImagen.visibility = View.VISIBLE
            }

            binding.btnEliminarPaso.setOnClickListener {
                onEliminarPaso(paso.numero)
            }

            binding.cardImagenPaso.setOnClickListener {
                onAgregarImagen(paso.numero)
            }

            binding.ivQuitarImagen.setOnClickListener {
                onQuitarImagen(paso.numero)
            }

            // Listener para cambios de texto
            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    onDescripcionChanged(paso.numero, s.toString())
                }
            }
            binding.etDescripcionPaso.addTextChangedListener(textWatcher)
        }
    }

    class PasoDiffCallback : DiffUtil.ItemCallback<PasoPreparacion>() {
        override fun areItemsTheSame(oldItem: PasoPreparacion, newItem: PasoPreparacion): Boolean {
            return oldItem.numero == newItem.numero
        }

        override fun areContentsTheSame(oldItem: PasoPreparacion, newItem: PasoPreparacion): Boolean {
            return oldItem.descripcion == newItem.descripcion && 
                   oldItem.numero == newItem.numero
        }
    }
}
