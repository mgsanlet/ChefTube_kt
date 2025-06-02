package com.mgsanlet.cheftube.ui.view.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.ViewRecipeCommentsBinding
import com.mgsanlet.cheftube.domain.model.DomainComment
import com.mgsanlet.cheftube.ui.adapter.CommentAdapter

/**
 * Vista personalizada que muestra los comentarios de una receta.
 *
 * Proporciona funcionalidad para:
 * - Mostrar/ocultar la sección de comentarios
 * - Enviar nuevos comentarios
 * - Reportar comentarios inapropiados
 * - Modo administrador para gestionar comentarios
 *
 * @property context Contexto de la aplicación
 * @property attrs Atributos XML personalizados
 * @property defStyleAttr Estilo por defecto
 */
class RecipeCommentsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ViewRecipeCommentsBinding = ViewRecipeCommentsBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    init {
        setUpListeners()
    }

    /**
     * Configura los listeners para los elementos interactivos de la vista.
     *
     * Incluye la lógica para expandir/contraer la sección de comentarios
     * y actualizar el ícono del botón correspondientemente.
     */
    private fun setUpListeners() {
        binding.expandToggleButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.commentEditText.requestFocus()
                binding.commentsBody.visibility = VISIBLE
                binding.expandToggleButton.setBackgroundResource(R.drawable.ic_arrow_v3_up_24)
            } else {
                binding.commentsBody.visibility = GONE
                binding.expandToggleButton.setBackgroundResource(R.drawable.ic_arrow_v3_down_24)
            }
        }
    }

    /**
     * Establece la lista de comentarios a mostrar.
     *
     * Configura el adaptador del RecyclerView con los comentarios proporcionados.
     * Si la lista está vacía, oculta el RecyclerView.
     *
     * @param comments Lista de comentarios a mostrar
     * @param fragmentManager Gestor de fragmentos necesario para mostrar diálogos
     * @param onCommentReportedListener Callback que se ejecuta cuando se reporta un comentario
     */
    fun setComments(
        comments: List<DomainComment>,
        fragmentManager: FragmentManager,
        onCommentReportedListener: (DomainComment) -> Unit = {}
    ) {
        if (comments.isEmpty()) {
            binding.commentRecycler.visibility = GONE
        } else {
            binding.commentRecycler.visibility = VISIBLE
            val layoutManager = LinearLayoutManager(context)
            layoutManager.reverseLayout = true
            layoutManager.stackFromEnd = true
            binding.commentRecycler.layoutManager = layoutManager
            binding.commentRecycler.adapter = CommentAdapter(
                context, 
                comments, 
                fragmentManager,
                onCommentReportedListener = onCommentReportedListener
            )
        }
    }

    /**
     * Establece el listener para el envío de comentarios.
     *
     * @param listener Función que se ejecuta cuando el usuario envía un comentario.
     *                 Recibe como parámetro el texto del comentario.
     */
    fun setOnCommentSentListener(listener: (String) -> Unit) {
        binding.sendCommentButton.setOnClickListener {
            val commentText = binding.commentEditText.text.toString()
            listener(commentText)
            binding.commentEditText.text?.clear()
        }
    }

    /**
     * Establece el listener para el botón de expandir/contraer.
     *
     * @param listener Función que se ejecuta cuando se intenta expandir la vista
     *                 (solo cuando la vista está contraída)
     */
    fun setOnExpandDownClickListener(listener: () -> Unit) {
        binding.expandToggleButton.setOnClickListener {
            if (binding.commentsBody.isVisible) {
                listener()
            }
        }
    }

    /**
     * Habilita el modo administrador en el adaptador de comentarios.
     *
     * Permite realizar acciones de administración como eliminar comentarios.
     * Si el adaptador no está configurado, no realiza ninguna acción.
     */
    fun setAdminMode() {
        try {
            val adapter = binding.commentRecycler.adapter as CommentAdapter
            adapter.updateAdminMode(true)
        } catch (_: Exception) { }
    }

}