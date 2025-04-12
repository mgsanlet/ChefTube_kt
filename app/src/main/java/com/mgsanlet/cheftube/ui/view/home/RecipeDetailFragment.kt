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
import com.mgsanlet.cheftube.ChefTubeApplication
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.databinding.FragmentRecipeDetailBinding
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeDetailViewModel
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeDetailViewModelFactory
import com.mgsanlet.cheftube.ui.viewmodel.home.RecipeState
import com.mgsanlet.cheftube.ui.viewmodel.home.TimerState

/**
 * Un fragmento que muestra los detalles de una receta, incluyendo su título, ingredientes,
 * pasos de preparación y un video incrustado (si está disponible). También incluye un temporizador de cuenta regresiva
 * para el tiempo de cocción o preparación.
 */
class RecipeDetailFragment : BaseFragment<FragmentRecipeDetailBinding, RecipeDetailViewModel>() {

    private val _viewModel: RecipeDetailViewModel by viewModels {
        val app by lazy { ChefTubeApplication.getInstance(requireContext()) }
        val recipeId = requireArguments().getString(ARG_RECIPE)
            ?: throw IllegalArgumentException("Recipe ID is required")
        RecipeDetailViewModelFactory(recipeId, app)
    }

    override fun defineViewModel(): RecipeDetailViewModel {
        return _viewModel
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentRecipeDetailBinding = FragmentRecipeDetailBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadRecipe() // Cargar la receta después de que el fragment esté creado
    }

    override fun setUpObservers() {
        viewModel.recipeState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RecipeState.Loading -> {
                    showLoading()
                }

                is RecipeState.Success -> {
                    setRecipeDetails(state.recipe)/* El progressBar se oculta cuando la webview terminó de cargar,
                       pero si no hay, debe ocultarse cuando la petición devuelva éxito */
                    if (binding.videoWebView.url == null) {
                        hideLoading()
                    }
                    hideProgressWhenVideoLoaded()
                }

                is RecipeState.Error -> {
                    hideLoading()
                    showError(state.message)
                }
            }
        }

        viewModel.timerState.observe(viewLifecycleOwner) { state ->
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

        viewModel.timeLeft.observe(viewLifecycleOwner) { time ->
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
                null -> showError("Not supported timer state")
            }
        }

        binding.timerTextView.setOnClickListener {
            if (viewModel.timerState.value == TimerState.Running) viewModel.pauseTimer()
            showSetTimerDialog()
        }
    }

    override fun setUpViewProperties() {
        setUpProgressBar(binding.progressBar)

    }

    private fun hideProgressWhenVideoLoaded() {
        if (binding.videoWebView.progress == 100) {
            hideLoading()
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                hideProgressWhenVideoLoaded()
            }, 200)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setRecipeDetails(recipe: Recipe?) {
        if (recipe != null) {
            binding.titleTextView.text = getString(recipe.ttlRId)
            // Configurar vídeo
            binding.videoWebView.settings.javaScriptEnabled = true
            val videoUrl = recipe.videoUrl
            binding.videoWebView.loadUrl(videoUrl)

            // Agregar ingredientes dinámicamente al contenedor de ingredientes
            fillIngredients(recipe)

            // Agregar pasos dinámicamente al contenedor de pasos
            fillSteps(recipe)
        }
    }

    private fun fillIngredients(recipe: Recipe) {
        binding.ingredientsLinearLayout.removeAllViews()
        for (ingredientId in recipe.getIngredientsResIds()) {
            val ingredientTextView = TextView(context)
            ingredientTextView.setText(ingredientId)
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
        binding.stepsLinearLayout.removeAllViews()
        for (stepId in recipe.getStepsResIds()) {
            val stepTextView = TextView(context)
            stepTextView.setText(stepId)
            stepTextView.setPadding(0, 4, 0, 2)
            if (context != null) {
                stepTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )
            }
            stepTextView.textSize = 12f
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

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recipeContent.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.recipeContent.visibility = View.VISIBLE
    }

    companion object {
        private const val ARG_RECIPE = "recipe"

        fun newInstance(recipeId: String): RecipeDetailFragment {
            val fragment = RecipeDetailFragment()
            val args = Bundle()
            args.putString(ARG_RECIPE, recipeId)
            fragment.arguments = args
            return fragment
        }
    }
}