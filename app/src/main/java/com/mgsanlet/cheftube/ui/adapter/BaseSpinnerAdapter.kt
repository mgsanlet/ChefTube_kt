package com.mgsanlet.cheftube.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mgsanlet.cheftube.databinding.SpinnerHeaderBinding
import com.mgsanlet.cheftube.databinding.SpinnerItemBinding

/**
 * Adaptador base para spinners personalizados.
 * Proporciona una implementación genérica para mostrar elementos en un Spinner
 * con diseños personalizados para la vista cerrada y desplegada.
 *
 * @property context Contexto de la aplicación
 * @property items Lista de cadenas que se mostrarán en el Spinner
 */
class BaseSpinnerAdapter(
    context: Context,
    private val items: List<String>
) : ArrayAdapter<String>(context, 0, items) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * Obtiene la vista que se muestra cuando el Spinner está cerrado.
     *
     * @param position Posición del elemento en el adaptador
     * @param convertView Vista reciclada, si está disponible
     * @param parent Vista padre a la que se adjuntará la vista
     * @return Vista que representa el elemento en la posición dada
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: SpinnerHeaderBinding
        val view: View

        if (convertView == null) {
            binding = SpinnerHeaderBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as SpinnerHeaderBinding
        }
        binding.spinnerHeader.text = items[position]

        return view
    }

    /**
     * Obtiene la vista desplegable que se muestra cuando el Spinner está abierto.
     *
     * @param position Posición del elemento en el adaptador
     * @param convertView Vista reciclada, si está disponible
     * @param parent Vista padre a la que se adjuntará la vista
     * @return Vista que representa el elemento en la posición dada en la lista desplegable
     */
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: SpinnerItemBinding
        val view: View

        if (convertView == null) {
            binding = SpinnerItemBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            view = convertView
            binding = view.tag as SpinnerItemBinding
        }

        binding.spinnerDropdownItem.text = items[position]
        return view
    }
}
