package com.mgsanlet.cheftube.ui.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.DialogReportBinding
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
    private var isAdminMode: Boolean = false
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
        if (isAdminMode) {
            binding.reportButton.visibility = Button.VISIBLE
            binding.reportButton.setOnClickListener {
                showReportDialog()
            }
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdminMode(isAdmin: Boolean) {
        this.isAdminMode = isAdmin
        notifyDataSetChanged()
    }

    private fun showReportDialog() {
        val dialogView = DialogReportBinding.inflate(LayoutInflater.from(mContext), null, false)
        val dialog = AlertDialog.Builder(mContext)
            .setView(dialogView.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (mContext.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        var selectedReason = ""

        dialogView.reasonRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedReason = when (checkedId) {
                R.id.inappropriateRadioButton -> mContext.getString(R.string.inappropriate)
                R.id.spamRadioButton -> mContext.getString(R.string.spam)
                R.id.violenceRadioButton -> mContext.getString(R.string.violence_reason)
                R.id.otherRadioButton -> mContext.getString(R.string.other_reason)
                else -> ""
            }

            dialogView.confirmButton.isEnabled = selectedReason.isNotEmpty()
        }

        dialogView.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialogView.confirmButton.setOnClickListener {
            if (selectedReason.isNotEmpty()) {
                // TODO: Handle report submission with selectedReason
                // For example: viewModel.reportComment(commentId, selectedReason)
                dialog.dismiss()
            }
        }

        dialogView.confirmButton.isEnabled = false

        dialog.show()
    }
}
