package com.mgsanlet.cheftube.ui.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Clase base abstracta para todos los fragmentos de la aplicación.
 * Proporciona funcionalidades comunes y estructura básica para la inicialización de vistas.
 *
 * @param T Tipo de binding generado para el layout del fragmento
 */
abstract class BaseFragment<T : ViewBinding> : Fragment() {

    /** Referencia al binding de la vista. Se limpia en onDestroyView para evitar memory leaks. */
    private var _binding: T? = null
    
    /**
     * Propiedad protegida que proporciona acceso no nulo al binding.
     * Lanza IllegalStateException si se accede cuando la vista no está disponible.
     */
    protected val binding get() = _binding!!

    /**
     * Se llama para crear la jerarquía de vistas asociada con el fragmento.
     * Inicializa el binding, configura observadores, listeners y propiedades de la vista.
     *
     * @param inflater El LayoutInflater usado para inflar la vista
     * @param container El ViewGroup padre al que se adjuntará la vista
     * @param savedInstanceState Estado previamente guardado de la instancia
     * @return La vista inflada para el fragmento
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = inflateViewBinding(inflater, container)
        setUpObservers()
        setUpListeners()
        setUpViewProperties()
        return binding.root
    }

    /**
     * Se llama cuando la vista del fragmento está a punto de ser destruida.
     * Limpia la referencia al binding para evitar memory leaks.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Método abstracto que debe implementarse para inflar y devolver el binding de la vista.
     *
     * @param inflater LayoutInflater usado para inflar la vista
     * @param container Contenedor padre de la vista
     * @return Instancia del binding inflado
     */
    protected abstract fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?): T

    /**
     * Configura los observadores de LiveData.
     * Se llama automáticamente en onCreateView.
     */
    protected open fun setUpObservers() {}

    /**
     * Configura los listeners de los elementos de la interfaz de usuario.
     * Se llama automáticamente en onCreateView.
     */
    protected open fun setUpListeners() {}

    /**
     * Configura propiedades iniciales de la vista.
     * Se llama automáticamente en onCreateView después de setUpListeners.
     */
    protected open fun setUpViewProperties() {}

    /**
     * Muestra un mensaje Toast con el texto proporcionado.
     *
     * @param message Texto a mostrar en el Toast
     */
    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Muestra un mensaje Toast con el texto del recurso proporcionado.
     *
     * @param messageRes ID del recurso de cadena a mostrar
     */
    protected fun showToast(@StringRes messageRes: Int) {
        showToast(getString(messageRes))
    }
}