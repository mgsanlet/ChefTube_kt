package com.mgsanlet.cheftube.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.ItemInactiveUserBinding
import com.mgsanlet.cheftube.domain.model.DomainUser

/**
 * Adaptador para mostrar una lista de usuarios inactivos en un RecyclerView.
 * Utiliza ListAdapter con DiffUtil para actualizaciones eficientes de la lista.
 */
class InactiveUsersAdapter : ListAdapter<DomainUser, InactiveUsersAdapter.InactiveUserViewHolder>(
    InactiveUserDiffCallback()
) {

    /**
     * Crea una nueva instancia de ViewHolder.
     *
     * @param parent El ViewGroup en el que se añadirá la nueva vista
     * @param viewType El tipo de vista del nuevo View
     * @return Un nuevo ViewHolder que contiene la vista del elemento de usuario inactivo
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InactiveUserViewHolder {
        val binding = ItemInactiveUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InactiveUserViewHolder(binding)
    }

    /**
     * Actualiza el contenido del ViewHolder en la posición dada.
     *
     * @param holder El ViewHolder que debe actualizarse
     * @param position La posición del elemento en el conjunto de datos
     */
    override fun onBindViewHolder(holder: InactiveUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder que contiene las vistas de un elemento de usuario inactivo.
     *
     * @property binding Binding que contiene las vistas del elemento
     */
    inner class InactiveUserViewHolder(
        private val binding: ItemInactiveUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        /**
         * Vincula los datos del usuario a las vistas.
         *
         * @param user Datos del usuario a mostrar
         */
        fun bind(user: DomainUser) {
            with(binding) {
                // Mostrar información del usuario
                textUsername.text = user.username
                textEmail.text = user.email
                
                // Mostrar días inactivos
                textInactiveDays.text = root.context.getString(
                    R.string.days_inactive_format,
                    user.inactiveDays
                )
            }
        }
    }
}

/**
 * Callback para calcular las diferencias entre dos listas de usuarios inactivos.
 * Mejora el rendimiento al actualizar solo los elementos que han cambiado.
 */
private class InactiveUserDiffCallback : DiffUtil.ItemCallback<DomainUser>() {
    /**
     * Comprueba si dos elementos representan el mismo objeto.
     *
     * @param oldItem Elemento antiguo
     * @param newItem Elemento nuevo
     * @return true si los elementos representan el mismo objeto, false en caso contrario
     */
    override fun areItemsTheSame(oldItem: DomainUser, newItem: DomainUser): Boolean =
        oldItem.id == newItem.id

    /**
     * Comprueba si dos elementos representan el mismo objeto.
     *
     * @param oldItem Elemento antiguo
     * @param newItem Elemento nuevo
     * @return true si los elementos representan el mismo objeto, false en caso contrario
     */
    override fun areContentsTheSame(
        oldItem: DomainUser,
        newItem: DomainUser
    ): Boolean {
        return oldItem.id == newItem.id
    }
}
