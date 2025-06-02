package com.mgsanlet.cheftube.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.ItemCommentBinding
import com.mgsanlet.cheftube.domain.model.DomainComment
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.loadUrlToCircle
import com.mgsanlet.cheftube.ui.view.home.ProfileFragment
import com.mgsanlet.cheftube.ui.view.home.RecipeDetailFragment
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Adaptador para mostrar una lista de comentarios en un RecyclerView.
 * Permite la navegación a los perfiles de los autores y reportar comentarios.
 *
 * @param mContext Contexto de la aplicación
 * @param commentList Lista de comentarios a mostrar
 * @param fragmentManager Gestor de fragmentos para la navegación
 * @param isAdminMode Indica si se muestra la funcionalidad de administrador
 * @param onCommentReportedListener Callback que se ejecuta cuando se reporta un comentario
 */
class CommentAdapter(
    private val mContext: Context,
    private val commentList: List<DomainComment>,
    private val fragmentManager: FragmentManager,
    private var isAdminMode: Boolean = false,
    internal var onCommentReportedListener: (DomainComment) -> Unit = {}
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    /**
     * ViewHolder que contiene las vistas de un elemento de comentario.
     *
     * @property binding Binding que contiene las vistas del elemento de comentario
     */
    inner class CommentViewHolder(var binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
     * Crea una nueva instancia de ViewHolder.
     *
     * @param parent El ViewGroup en el que se añadirá la nueva vista
     * @param viewType El tipo de vista del nuevo View
     * @return Un nuevo ViewHolder que contiene la vista del elemento de comentario
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(mContext), parent, false
        )
        return CommentViewHolder(binding)
    }

    /**
     * Actualiza el contenido del ViewHolder en la posición dada.
     *
     * @param holder El ViewHolder que debe actualizarse
     * @param position La posición del elemento en el conjunto de datos
     */
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        val binding = holder.binding

        binding.commentContentTextView.text = comment.content

        val dateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(comment.timestamp), ZoneId.systemDefault())
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy   HH:mm")
        binding.timestampTextView.text = dateTime.format(dateFormatter)

        binding.authorTextView.text = comment.author.username
        if (comment.author.profilePictureUrl.isNotBlank()) {
            binding.authorImageView.loadUrlToCircle(comment.author.profilePictureUrl, mContext)
        }
        binding.authorImageView.setOnClickListener {
            navToAuthorProfile(comment.author.id)
        }
        binding.authorTextView.setOnClickListener {
            navToAuthorProfile(comment.author.id)
        }
        if (isAdminMode) { binding.reportButton.visibility = Button.VISIBLE }

        binding.reportButton.setOnClickListener { onCommentReportedListener(comment) }

    }

    /**
     * Devuelve el número total de elementos en el conjunto de datos.
     *
     * @return Número total de comentarios
     */
    override fun getItemCount(): Int = commentList.size


    /**
     * Navega al perfil del autor del comentario.
     *
     * @param authorId ID del autor del comentario
     */
    private fun navToAuthorProfile(authorId: String) {
        // Obtener el fragmento visible actual
        val currentFragment =
            fragmentManager.findFragmentById(R.id.fragmentContainerView) as RecipeDetailFragment?
        if (currentFragment != null && currentFragment.isVisible) {
            val profileFragment = ProfileFragment.newInstance(authorId)

            FragmentNavigator.loadFragmentInstance(
                null, currentFragment, profileFragment, R.id.fragmentContainerView
            )
        }
    }

    /**
     * Actualiza el modo de administrador del adaptador.
     * Cuando está activado, muestra botones adicionales para la moderación.
     *
     * @param isAdmin Indica si se debe activar el modo administrador
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateAdminMode(isAdmin: Boolean) {
        this.isAdminMode = isAdmin
        notifyDataSetChanged()
    }
}
