package com.mgsanlet.cheftube.view.ui.home

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.data.model.Recipe
import com.mgsanlet.cheftube.databinding.FragmentRecipeDetailBinding

/**
 * Un fragmento que muestra los detalles de una receta, incluyendo su título, ingredientes,
 * pasos de preparación y un video incrustado (si está disponible). También incluye un temporizador de cuenta regresiva
 * para el tiempo de cocción o preparación.
 */
class RecipeDetailFragment : Fragment() {
    
    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    // -Variables del cronómetro-
    private var mCountDownTimer: CountDownTimer? = null
    private var mTimeLeftInMillis: Long = 0
    private var mIsTimerRunning = false
    private lateinit var mMediaPlayer: MediaPlayer
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        var recipe: Recipe? = null

        arguments?.let {
            recipe = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireArguments().getSerializable(ARG_RECIPE, Recipe::class.java) //TODO cambiar a parcelable
            }else{
                @Suppress("DEPRECATION") // Solo se usará para versiones antiguas
                requireArguments().getSerializable(ARG_RECIPE) as Recipe?
            }
        }

        setRecipeDetails(recipe)

        // Listeners
        binding.startPauseButton.setOnClickListener { startPauseTimer() }
        binding.timerTextView.setOnClickListener { showSetTimerDialog() }

        return binding.root
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
        for (ingredientId in recipe.getIngredientsResIds()) {
            val ingredientTextView = TextView(context)
            ingredientTextView.setText(ingredientId)
            if (context != null) {
                ingredientTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
            ingredientTextView.textSize = 16f
            binding.ingredientsLinearLayout.addView(ingredientTextView)
        }
    }

    private fun fillSteps(recipe: Recipe) {
        for (stepId in recipe.getStepsResIds()) {
            val stepTextView = TextView(context)
            stepTextView.setText(stepId)
            stepTextView.setPadding(0, 4, 0, 2)
            if (context != null) {
                stepTextView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            }
            stepTextView.textSize = 12f
            binding.stepsLinearLayout.addView(stepTextView)
        }
    }

    /**
     * Alterna el estado del temporizador entre iniciado y pausado. Si el temporizador está en
     * ejecución, se pausará. Si el temporizador está pausado, se iniciará.
     */
    private fun startPauseTimer() {
        if (mIsTimerRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    /**
     * Inicia el temporizador de cuenta regresiva. Crea una nueva instancia de CountDownTimer y
     * la inicializa con el tiempo restante. Actualiza la visualización del temporizador cada
     * segundo y maneja la finalización del temporizador.
     */
    private fun startTimer() {
        mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                updateTimer()
            }

            override fun onFinish() {
                mIsTimerRunning = false
                binding.startPauseButton.setText(R.string.start)
                playAlarmSound()
            }
        }.start()

        mIsTimerRunning = true
        binding.startPauseButton.setText(R.string.pause)
    }

    /**
     * Pausa el temporizador que se está ejecutando actualmente. Cancela la cuenta regresiva y
     * actualiza la interfaz de usuario para reflejar el estado de pausa.
     */
    private fun pauseTimer() {
        mCountDownTimer!!.cancel()
        mIsTimerRunning = false
        binding.startPauseButton.setText(R.string.start)
    }

    /**
     * Actualiza la visualización del temporizador con el tiempo restante actual.
     * Convierte milisegundos a formato de minutos y segundos (MM:SS) y lo muestra en el
     * TextView del temporizador.
     */
    @SuppressLint("DefaultLocale") // El string no depende de Locale
    private fun updateTimer() {
        val minutes = (mTimeLeftInMillis / 1000).toInt() / 60
        val seconds = (mTimeLeftInMillis / 1000).toInt() % 60

        val timeLeftFormatted = String.format("%02d:%02d", minutes, seconds)
        binding.timerTextView.text = timeLeftFormatted
    }

    /**
     * Muestra un diálogo que permite al usuario establecer la duración del temporizador.
     * El diálogo contiene campos de entrada para minutos y segundos. Cuando el usuario confirma,
     * el temporizador se actualiza con la nueva duración y la visualización se refresca.
     */
    private fun showSetTimerDialog() {
        val dialogBuilder = AlertDialog.Builder(
            requireContext()
        )
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_set_timer, null)
        dialogBuilder.setView(dialogView)

        val minutesInput = dialogView.findViewById<EditText>(R.id.minutesInput)
        val secondsInput = dialogView.findViewById<EditText>(R.id.secondsInput)

        dialogBuilder.setPositiveButton(R.string.set) { _: DialogInterface?, _: Int ->
            val minutesString = minutesInput.text.toString()
            val secondsString = secondsInput.text.toString()

            val minutes = if (minutesString.isEmpty()) 0 else minutesString.toInt()
            val seconds = if (secondsString.isEmpty()) 0 else secondsString.toInt()

            // Convertir a milisegundos y establecer el temporizador
            mTimeLeftInMillis = (minutes * 60L + seconds) * 1000
            updateTimer() // Actualizar el temporizador mostrado
        }

        dialogBuilder.setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.dismiss() }

        val timerDialog = dialogBuilder.create()
        timerDialog.show()
    }

    /**
     * Reproduce el sonido de alarma cuando el temporizador se completa. Utiliza MediaPlayer para
     * reproducir el recurso de sonido. Libera automáticamente los recursos de MediaPlayer después
     * de que la reproducción termine.
     */
    private fun playAlarmSound() {
        // Inicializar MediaPlayer para reproducir el sonido de alarma
        mMediaPlayer = MediaPlayer.create(context, R.raw.alarm_clock)
        mMediaPlayer.start()

        // Liberar los recursos de MediaPlayer después de que la reproducción termine
        mMediaPlayer.setOnCompletionListener { mp: MediaPlayer ->
            mp.release()
        }
    }

    companion object {
        private const val ARG_RECIPE = "recipe"

        fun newInstance(recipe: Recipe?): RecipeDetailFragment {
            val fragment = RecipeDetailFragment()
            val args = Bundle()
            args.putSerializable(ARG_RECIPE, recipe)
            fragment.arguments = args
            return fragment
        }
    }
}