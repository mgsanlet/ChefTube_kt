package com.mgsanlet.cheftube.ui.view.home

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.flexbox.FlexboxLayout
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentRecipeDetailBinding
import com.mgsanlet.cheftube.ui.util.Constants.ARG_RECIPE
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeDetailViewModel
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeState
import com.mgsanlet.cheftube.ui.viewmodel.home.TimerState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.mgsanlet.cheftube.domain.model.DomainRecipe as Recipe

/**
 * Un fragmento que muestra los detalles de una receta, incluyendo su título, ingredientes,
 * pasos de preparación y un video incrustado (si está disponible). También incluye un temporizador de cuenta regresiva
 * para el tiempo de cocción o preparación.
 */
@AndroidEntryPoint
class RecipeDetailFragment @Inject constructor() : BaseFragment<FragmentRecipeDetailBinding>() {

    private val viewModel: RecipeDetailViewModel by viewModels()
    private var isToggleInitialization: Boolean = true

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
                }

                is RecipeState.Error -> {
                    showLoading(false)
                    Toast.makeText(
                        requireContext(),
                        state.error.asMessage(requireContext()),
                        Toast.LENGTH_LONG
                    ).show()
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

    override fun setUpViewProperties() {
        binding.progressBar.setCustomStyle(requireContext())
    }

    private fun hideProgressWhenVideoLoaded() {
        if (binding.videoWebView.progress == 100) {
            showLoading(false)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                hideProgressWhenVideoLoaded()
            }, 200)
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
        // Configurar vídeo
        binding.videoWebView.settings.javaScriptEnabled = true
        val videoUrl = recipe.videoUrl
        binding.videoWebView.loadUrl(videoUrl)

        binding.difficultyTextView.setDifficulty(2)
        binding.durationTextView.setDuration(recipe.durationMinutes)
        binding.favouriteToggle.isChecked = viewModel.isFavourite
        isToggleInitialization = false
        binding.favouriteNumberTextView.text = recipe.favouriteCount.toString()

        fillCategories(recipe)
        // Agregar ingredientes dinámicamente al contenedor de ingredientes
        fillIngredients(recipe)
        // Agregar pasos dinámicamente al contenedor de pasos
        fillSteps(recipe)
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
                    ContextCompat.getDrawable(requireContext(), R.drawable.base_field_shapes)
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
            binding.progressBar.visibility = View.VISIBLE
            binding.recipeContent.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.recipeContent.visibility = View.VISIBLE
        }
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