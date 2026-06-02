package com.example.recetarioboliviano.vista.adaptadores

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.recetarioboliviano.databinding.ItemUsuarioBinding
import com.example.recetarioboliviano.modelo.entidades.Usuario
import com.example.recetarioboliviano.modelo.util.ImageHelper

class UsuarioAdapter(
    private val onUsuarioClick: (Usuario) -> Unit,
    private val onEliminarClick: (Usuario) -> Unit
) : ListAdapter<Usuario, UsuarioAdapter.UsuarioViewHolder>(UsuarioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val binding = ItemUsuarioBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UsuarioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UsuarioViewHolder(
        private val binding: ItemUsuarioBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onUsuarioClick(getItem(position))
                }
            }

            binding.btnEliminar.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEliminarClick(getItem(position))
                }
            }
        }

        fun bind(usuario: Usuario) {
            binding.tvNombre.text = usuario.nombre
            binding.tvEmail.text = "Rol: ${usuario.role}"
            binding.tvDetalles.text = "${usuario.departamento ?: "Sin depto"}, ${usuario.pais}"
            
            ImageHelper.cargarAvatar(binding.ivAvatar, usuario.avatarUri)
        }
    }

    class UsuarioDiffCallback : DiffUtil.ItemCallback<Usuario>() {
        override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
            return oldItem == newItem
        }
    }
}
