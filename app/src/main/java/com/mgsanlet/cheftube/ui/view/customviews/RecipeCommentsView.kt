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

    private fun setUpListeners(){
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

    fun setComments(comments: List<DomainComment>, fragmentManager: FragmentManager) {
        if (comments.isEmpty()) {
            binding.commentRecycler.visibility = GONE
        }else{
            binding.commentRecycler.visibility = VISIBLE
            val layoutManager = LinearLayoutManager(context)
            layoutManager.reverseLayout = true
            layoutManager.stackFromEnd = true
            binding.commentRecycler.layoutManager = layoutManager
            binding.commentRecycler.adapter = CommentAdapter(context, comments, fragmentManager)
        }
    }

    fun setOnCommentSentListener(listener: (String) -> Unit) {
        binding.sendCommentButton.setOnClickListener {
            val commentText = binding.commentEditText.text.toString()
            listener(commentText)
            binding.commentEditText.text?.clear()
        }
    }

    fun setOnExpandDownClickListener(listener: () -> Unit) {
        binding.expandToggleButton.setOnClickListener {
            if (binding.commentsBody.isVisible) {
                listener()
            }
        }
    }

    fun setAdminMode(){
        val adapter = binding.commentRecycler.adapter as CommentAdapter
        adapter.updateAdminMode(true)
    }

}