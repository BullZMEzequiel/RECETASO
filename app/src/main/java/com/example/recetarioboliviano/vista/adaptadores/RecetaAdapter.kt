package com.example.recetarioboliviano.vista.adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recetarioboliviano.R
import com.example.recetarioboliviano.databinding.ItemRecetaBinding
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.modelo.util.ImageHelper

class RecetaAdapter(
    private val onItemClick: (Receta) -> Unit,
    private val onFavoritoClick: (Receta) -> Unit
) : ListAdapter<Receta, RecetaAdapter.RecetaViewHolder>(RecetaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val binding = ItemRecetaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecetaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecetaViewHolder(
        private val binding: ItemRecetaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.btnFavorito.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavoritoClick(getItem(position))
                }
            }
        }

        fun bind(receta: Receta) {
            binding.tvTitulo.text = receta.titulo
            binding.tvDepartamento.text = receta.departamento
            binding.tvTiempo.text = "${receta.tiempoPreparacion ?: 0} min"
            binding.tvCategoria.text = receta.categoria

            val iconRes = if (receta.esFavorito) {
                R.drawable.ic_favorite
            } else {
                R.drawable.ic_favorite_border
            }
            binding.btnFavorito.setIconResource(iconRes)

            binding.chipUsuario.visibility =
                if (receta.visibilidad == com.example.recetarioboliviano.modelo.entidades.RecetaVisibilidad.PRIVADA) View.VISIBLE else View.GONE

            // CORRECCIÓN para evitar crash
            ImageHelper.cargarImagen(binding.ivReceta, receta.imagenUri.orEmpty())
        }
    } // 👈 FALTABA ESTE CIERRE

    class RecetaDiffCallback : DiffUtil.ItemCallback<Receta>() {
        override fun areItemsTheSame(oldItem: Receta, newItem: Receta): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Receta, newItem: Receta): Boolean {
            return oldItem == newItem
        }
    }
}