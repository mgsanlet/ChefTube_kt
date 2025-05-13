package com.mgsanlet.cheftube.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
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

class CommentAdapter(
    private val mContext: Context,
    private val commentList: List<DomainComment>,
    private val fragmentManager: FragmentManager,
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(var binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(mContext), parent, false
        )
        return CommentViewHolder(binding)
    }

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
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

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
}
