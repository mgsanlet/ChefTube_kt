package com.mgsanlet.cheftube.ui.view.home

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.RecipeCommentsViewBinding
import com.mgsanlet.cheftube.domain.model.DomainComment
import com.mgsanlet.cheftube.ui.adapter.CommentAdapter

class RecipeCommentsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: RecipeCommentsViewBinding = RecipeCommentsViewBinding.inflate(
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
                binding.expandToggleButton.setBackgroundResource(R.drawable.ic_expand_up_25)
            } else {
                binding.commentsBody.visibility = GONE
                binding.expandToggleButton.setBackgroundResource(R.drawable.ic_expand_down_25)
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
}