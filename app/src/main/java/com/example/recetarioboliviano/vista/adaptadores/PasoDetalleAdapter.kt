package com.example.recetarioboliviano.vista.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recetarioboliviano.databinding.ItemPasoDetalleBinding
import com.example.recetarioboliviano.modelo.entidades.PasoPreparacion
import com.example.recetarioboliviano.modelo.util.ImageHelper

/**
 * Adaptador para mostrar los pasos de preparación en la pantalla de detalle.
 */
class PasoDetalleAdapter : ListAdapter<PasoPreparacion, PasoDetalleAdapter.PasoViewHolder>(PasoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasoViewHolder {
        val binding = ItemPasoDetalleBinding.inflate(
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
        private val binding: ItemPasoDetalleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(paso: PasoPreparacion) {
            binding.tvNumeroPaso.text = paso.numero.toString()
            binding.tvDescripcionPaso.text = paso.descripcion

            // Imagen desactivada según esquema SQL
            binding.cardImagenPaso.visibility = View.GONE
        }
    }

    class PasoDiffCallback : DiffUtil.ItemCallback<PasoPreparacion>() {
        override fun areItemsTheSame(oldItem: PasoPreparacion, newItem: PasoPreparacion): Boolean {
            return oldItem.numero == newItem.numero
        }

        override fun areContentsTheSame(oldItem: PasoPreparacion, newItem: PasoPreparacion): Boolean {
            return oldItem == newItem
        }
    }
}
