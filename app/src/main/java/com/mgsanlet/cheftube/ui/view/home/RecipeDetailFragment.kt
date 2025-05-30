package com.mgsanlet.cheftube.ui.view.home

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexboxLayout
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.DialogReportBinding
import com.mgsanlet.cheftube.databinding.FragmentRecipeDetailBinding
import com.mgsanlet.cheftube.domain.model.DomainComment
import com.mgsanlet.cheftube.ui.util.Constants.ARG_RECIPE
import com.mgsanlet.cheftube.ui.util.Constants.URI_MAIL_TO_SCHEME
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.loadUrlToCircle
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.view.dialogs.LoadingDialog
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeDetailViewModel
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeState
import com.mgsanlet.cheftube.ui.viewmodel.home.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe
import androidx.core.net.toUri

/**
 * Un fragmento que muestra los detalles de una receta, incluyendo su título, ingredientes,
 * pasos de preparación y un video incrustado (si está disponible). También incluye un temporizador de cuenta regresiva
 * para el tiempo de cocción o preparación.
 */
@AndroidEntryPoint
class RecipeDetailFragment @Inject constructor() : BaseFragment<FragmentRecipeDetailBinding>() {

    private val viewModel: RecipeDetailViewModel by viewModels()
    private var isToggleInitialization: Boolean = true
    private var progressCheckJob: Job? = null

    override fun onResume() {
        super.onResume()
        isToggleInitialization = true
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentRecipeDetailBinding = FragmentRecipeDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Cargar la receta después de que el fragment esté creado
        viewModel.loadRecipe(arguments?.getString(ARG_RECIPE) ?: "")
    }

    override fun setUpObservers() {
        viewModel.recipeState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RecipeState.Loading -> {
                    showLoading(true)
                }

                is RecipeState.Success -> {
                    setRecipeDetails(state.recipe)
                    /* El progressBar se oculta cuando la webview terminó de cargar,
                       pero si no hay, debe ocultarse cuando la petición devuelva éxito */
                    if (binding.videoWebView.url == null) {
                        showLoading(false)
                    }
                    hideProgressWhenVideoLoaded()
                    state.recipe.author?.let { setAuthorTagListener(it.id) }
                    viewModel.isCurrentUserAdmin()
                }

                is RecipeState.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        state.error.asMessage(requireContext()),
                        Toast.LENGTH_LONG
                    ).show()
                }

                RecipeState.DeleteSuccess -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.recipe_deleted),
                        Toast.LENGTH_LONG
                    ).show()
                    FragmentNavigator.loadFragment(
                        null,
                        this,
                        RecipeFeedFragment(),
                        R.id.fragmentContainerView
                    )
                }
            }
        }

        viewModel.timerState.observe(viewLifecycleOwner)
        { state ->
            when (state) {
                TimerState.Initial -> {
                    binding.startPauseButton.setText(R.string.start)
                }

                TimerState.Running -> {
                    binding.startPauseButton.setText(R.string.pause)
                }

                TimerState.Paused -> {
                    binding.startPauseButton.setText(R.string.start)
                }

                TimerState.Finished -> {
                    binding.startPauseButton.setText(R.string.start)
                    playAlarmSound()
                }
            }
        }

        viewModel.timeLeft.observe(viewLifecycleOwner)
        { time ->
            binding.timerTextView.text = time
        }

        viewModel.isUserAdmin.observe(viewLifecycleOwner) { isAdmin ->
            if (isAdmin) {
                binding.editButton.visibility = View.INVISIBLE
                binding.reportButton.visibility = View.VISIBLE
                binding.commentsView.setAdminMode()
            }
        }
    }

    override fun setUpListeners() {
        binding.startPauseButton.setOnClickListener {
            when (viewModel.timerState.value) {
                TimerState.Initial, TimerState.Finished -> {
                    if (viewModel.timeLeftInMillis >= 1000) {
                        viewModel.startTimer(viewModel.timeLeftInMillis)
                    } else {
                        showSetTimerDialog()
                    }
                }

                TimerState.Running -> viewModel.pauseTimer()
                TimerState.Paused -> viewModel.startTimer(viewModel.timeLeftInMillis)
                null -> {}
            }
        }

        binding.timerTextView.setOnClickListener {
            if (viewModel.timerState.value == TimerState.Running) viewModel.pauseTimer()
            showSetTimerDialog()
        }

        binding.favouriteToggle.setOnCheckedChangeListener { _, isChecked ->
            if (!isToggleInitialization) {
                viewModel.alternateFavourite(isChecked)
            }
        }

        binding.editButton.setOnClickListener {
            try {
                val instance = RecipeFormFragment.newInstance(arguments?.getString(ARG_RECIPE)!!)
                FragmentNavigator.loadFragmentInstance(
                    null,
                    this,
                    instance,
                    R.id.fragmentContainerView
                )
            } catch (_: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.unknown_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.commentsView.setOnCommentSentListener { comment ->
            if (comment.isBlank()) return@setOnCommentSentListener
            viewModel.postComment(comment)
        }

        binding.shareButton.setOnClickListener {
            viewModel.recipeState.value?.let { state ->
                if (state is RecipeState.Success) {
                    shareRecipe(state.recipe)
                }
            }
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            binding.commentsView.setOnExpandDownClickListener {
                scrollToCommentsView()
            }
        }

        binding.reportButton.setOnClickListener {
            viewModel.recipeState.value?.let { state ->
                if (state is RecipeState.Success) {
                    showReportDialog(state.recipe)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun scrollToCommentsView() {
        binding.scrollView.scrollToDescendant(binding.commentsView)
    }

    fun setAuthorTagListener(authorId: String) {
        binding.authorTag.setOnClickListener {
            if (authorId.isBlank()) return@setOnClickListener
            val instance = ProfileFragment.newInstance(authorId)
            FragmentNavigator.loadFragmentInstance(
                null,
                this,
                instance,
                R.id.fragmentContainerView
            )

        }
    }

    private fun hideProgressWhenVideoLoaded() {
        // Cancel any existing progress check
        progressCheckJob?.cancel()

        progressCheckJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                if (binding.videoWebView.progress == 100) {
                    showLoading(false)
                    break
                }
                delay(200)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setRecipeDetails(recipe: Recipe) {
        if (recipe.author == null) {
            binding.authorTag.visibility = View.GONE
        }
        if (recipe.videoUrl.isBlank()) {
            binding.videoFrame.visibility = View.GONE
        }
        binding.authorTextView.text = recipe.author?.username ?: ""
        binding.titleTextView.text = recipe.title

        // Cargar imagen de perfil del autor
        recipe.author?.profilePictureUrl?.takeIf { it.isNotBlank() }?.let {
            binding.authorImageView.loadUrlToCircle(it, requireContext())
        }

        // Configurar vídeo
        binding.videoWebView.settings.javaScriptEnabled = true
        binding.videoWebView.loadUrl(recipe.videoUrl)

        binding.difficultyTextView.setDifficulty(recipe.difficulty)
        binding.durationTextView.setDuration(recipe.durationMinutes)
        binding.favouriteToggle.isChecked = viewModel.isFavourite
        binding.editButton.visibility = if (viewModel.isRecipeByAuthor) View.VISIBLE else View.GONE
        isToggleInitialization = false
        binding.favouriteNumberTextView.text = recipe.favouriteCount.toString()

        // Agregar items de listas de forma dinámica
        fillCategories(recipe)
        fillIngredients(recipe)
        fillSteps(recipe)
        fillComments(recipe)
    }

    @SuppressLint("SetTextI18n") // No es necesario traducir #
    private fun fillCategories(recipe: Recipe) {
        binding.categoryContainer.removeAllViews()
        recipe.categories.forEach {
            val categoryTextView = TextView(context).apply {
                text = "#$it"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                textSize = 18f
                background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.shape_round_corner_15)
                backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.dark_green)
                setPadding(24, 8, 24, 8)
            }

            // Crear LayoutParams para establecer márgenes
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 0, 8, 24)
            }

            categoryTextView.layoutParams = layoutParams

            // Agregar el TextView al contenedor
            binding.categoryContainer.addView(categoryTextView)
        }
    }

    private fun fillIngredients(recipe: Recipe) {
        binding.ingredientsLinearLayout.removeAllViews()
        for (ingredient in recipe.ingredients) {
            val ingredientTextView = TextView(context)
            ingredientTextView.text = ingredient
            if (context != null) {
                ingredientTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )
            }
            ingredientTextView.textSize = 16f
            binding.ingredientsLinearLayout.addView(ingredientTextView)
        }
    }

    private fun fillSteps(recipe: Recipe) {
        var index = 1
        binding.stepsLinearLayout.removeAllViews()
        for (step in recipe.steps) {
            val stepTextView = TextView(context)
            stepTextView.text = getString(R.string.step_formatted, index++.toString(), step)
            stepTextView.setPadding(0, 4, 0, 2)
            if (context != null) {
                stepTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )
            }
            stepTextView.textSize = 16f
            binding.stepsLinearLayout.addView(stepTextView)
        }
    }

    private fun fillComments(recipe: Recipe) {
        binding.commentsView.setComments(
            recipe.comments,
            parentFragmentManager,
            onCommentReportedListener = { comment ->
                showReportDialog(comment)
            }
        )
    }

    private fun shareRecipe(recipe: Recipe) {
        val shareText = buildString {
            // Título y encabezado
            appendLine("*${recipe.title}*")
            appendLine("${getString(R.string.recipe_shared_from_cheftube)}\n")

            // Detalles de la receta
            appendLine("⏱️ ${getFormattedRecipeDuration(recipe)}")
            recipe.author?.username?.let { username ->
                appendLine("👨‍🍳 *${getString(R.string.author)}*: $username")
            }

            // Categorías
            if (recipe.categories.isNotEmpty()) {
                appendLine("\n🏷️ *${getString(R.string.categories)}*")
                recipe.categories.forEach { category ->
                    appendLine("• #$category")
                }
            }

            // Ingredientes
            appendLine("\n🛒 *${getString(R.string.ingredients)}*")
            recipe.ingredients.forEach { ingredient ->
                appendLine("• $ingredient")
            }

            // Pasos
            appendLine("\n📝 *${getString(R.string.steps)}*")
            recipe.steps.forEachIndexed { index, step ->
                appendLine("\n*${index + 1}.* $step")
            }
            // Video
            if (recipe.videoUrl.isNotBlank()) {
                val videoId = extractYouTubeIdFromEmbed(recipe.videoUrl)
                if (videoId != null) {
                    appendLine("\n▶️ https://www.youtube.com/watch?v=$videoId")
                }
            }
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_SUBJECT,
                "${getString(R.string.recipe_shared_from_cheftube)}: ${recipe.title}"
            )
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_recipe_using)))
    }

    private fun getFormattedRecipeDuration(recipe: Recipe): String {
        val currentHours = recipe.durationMinutes / 60
        val currentMinutes = recipe.durationMinutes % 60
        return if (currentHours > 0) {
            if (currentMinutes > 0) {
                getString(R.string.duration_hours_minutes, currentHours, currentMinutes)
            } else {
                getString(R.string.duration_hours, currentHours)
            }
        } else {
            getString(R.string.duration_minutes, currentMinutes)
        }
    }

    private fun extractYouTubeIdFromEmbed(url: String): String? {
        return url.substringAfter("embed/").substringBefore("?")
    }

    /**
     * Muestra un diálogo que permite al usuario establecer la duración del temporizador.
     * El diálogo contiene campos de entrada para minutos y segundos. Cuando el usuario confirma,
     * el temporizador se actualiza con la nueva duración y la visualización se refresca.
     */
    private fun showSetTimerDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_set_timer, null)
        dialogBuilder.setView(dialogView)

        val minutesInput = dialogView.findViewById<EditText>(R.id.minutesInput)
        val secondsInput = dialogView.findViewById<EditText>(R.id.secondsInput)

        dialogBuilder.setPositiveButton(R.string.set) { _, _ ->
            val minutes = minutesInput.text.toString().toIntOrNull() ?: 0
            val seconds = secondsInput.text.toString().toIntOrNull() ?: 0
            val timeInMillis = (minutes * 60L + seconds) * 1000

            viewModel.setTime(timeInMillis)
        }

        dialogBuilder.setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
        }

        dialogBuilder.create().show()
    }

    /**
     * Reproduce el sonido de alarma cuando el temporizador se completa. Utiliza MediaPlayer para
     * reproducir el recurso de sonido. Libera automáticamente los recursos de MediaPlayer después
     * de que la reproducción termine.
     */
    private fun playAlarmSound() {
        // Inicializar MediaPlayer para reproducir el sonido de alarma
        MediaPlayer.create(requireContext(), R.raw.alarm_clock).apply {
            // Liberar los recursos de MediaPlayer después de que la reproducción termine
            setOnCompletionListener {
                release()
            }
            start()
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.recipeContent.visibility = View.GONE
            LoadingDialog.show(requireContext(), parentFragmentManager)
        } else {
            binding.recipeContent.visibility = View.VISIBLE
            LoadingDialog.dismiss(parentFragmentManager)
        }
    }

    private fun showReportDialog(reportedEntity: Any) {
        if (reportedEntity !is Recipe && reportedEntity !is DomainComment) return

        val dialogView =
            DialogReportBinding.inflate(LayoutInflater.from(requireContext()), null, false)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (requireContext().resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        var selectedReason = requireContext().getString(R.string.other_reason)

        dialogView.reasonRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedReason = when (checkedId) {
                R.id.inappropriateRadioButton -> requireContext().getString(R.string.inappropriate)
                R.id.spamRadioButton -> requireContext().getString(R.string.spam)
                R.id.violenceRadioButton -> requireContext().getString(R.string.violence_reason)
                R.id.otherRadioButton -> requireContext().getString(R.string.other_reason)
                else -> ""
            }

            dialogView.confirmButton.isEnabled = selectedReason.isNotEmpty()
        }

        dialogView.cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialogView.confirmButton.setOnClickListener {
            if (selectedReason.isNotEmpty()) {

                if (reportedEntity is Recipe) {
                    viewModel.deleteRecipe(reportedEntity)
                } else if (reportedEntity is DomainComment) {
                    viewModel.deleteComment(reportedEntity)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.comment_deleted),
                        Toast.LENGTH_LONG
                    ).show()
                }
                dialog.dismiss()
                sendReportEmail(
                    reportedEntity,
                    selectedReason
                )
            }
        }

        dialogView.confirmButton.isEnabled = false

        dialog.show()
    }

    private fun sendReportEmail(reportedEntity: Any, reason: String) {
        var email = ""
        var subject = ""
        var content = ""

        if (reportedEntity is Recipe) {
            email = reportedEntity.author?.email ?: ""
            subject = getString(R.string.report_subject, getString(R.string.recipe))
            content = getString(R.string.recipe_report_content, reportedEntity.title, reason)
        } else if (reportedEntity is DomainComment) {
            email = reportedEntity.author.email
            subject = getString(R.string.report_subject, getString(R.string.comment))
            content = getString(R.string.comment_report_content, reportedEntity.content, reason)
        } else {
            return
        }

        if (email.isBlank()) {
            Toast.makeText(requireContext(),
                getString(R.string.error_author_s_email_not_found), Toast.LENGTH_LONG)
                .show()
            return
        }

        // Encode subject and content for URI query parameters
        val encodedSubject = Uri.encode(subject)
        val encodedContent = Uri.encode(content)

        val uriString = "mailto:$email?subject=$encodedSubject&body=$encodedContent"
        val emailUri = uriString.toUri()

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = emailUri
        }

        if (emailIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(requireContext(), getString(R.string.no_email_app), Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onDestroyView() {
        progressCheckJob?.cancel()
        progressCheckJob = null
        LoadingDialog.dismiss(parentFragmentManager)
        super.onDestroyView()
    }

    companion object {

        fun newInstance(recipeId: String): RecipeDetailFragment {
            val fragment = RecipeDetailFragment()
            val args = Bundle()
            args.putString(ARG_RECIPE, recipeId)
            fragment.arguments = args
            return fragment
        }
    }
}