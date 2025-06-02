package com.mgsanlet.cheftube.ui.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Clase de utilidad para gestionar transacciones de fragmentos en una AppCompatActivity.
 * Proporciona métodos para cargar fragmentos en contenedores específicos,
 * manejando tanto fragmentos con argumentos como sin ellos.
 */
object FragmentNavigator {
    /**
     * Reemplaza el fragmento actual con un nuevo fragmento en el contenedor especificado.
     * El nuevo fragmento se añadirá a la pila de retroceso.
     * Debe usarse cuando no se necesitan argumentos para el fragmento.
     *
     * @param activity Actividad que aloja la transacción del fragmento (puede ser nulo).
     * @param thisFr Fragmento actual (puede ser nulo).
     * @param fragment Fragmento que se cargará.
     * @param containerId ID del contenedor donde se colocará el fragmento.
     * @throws IllegalStateException Si no se puede obtener un FragmentManager válido.
     */
    fun loadFragment(
        activity: AppCompatActivity?,
        thisFr: Fragment?,
        fragment: Fragment,
        containerId: Int
    ) {
        // Tomando el FragmentManager para manejar las transacciones de fragmentos
        val fragmentManager = configFrManager(activity, thisFr)
        check(fragmentManager != null) { "Bad use of FragmentNavigator.loadFragment()" }

        fragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(containerId, fragment.javaClass, null)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Reemplaza el fragmento actual con una nueva instancia del fragmento especificado
     * en el contenedor dado. El fragmento se añade a la pila de retroceso.
     * Debe usarse cuando se necesitan argumentos para el fragmento.
     *
     * @param activity Actividad que aloja la transacción del fragmento (puede ser nulo).
     * @param thisFr Fragmento actual (puede ser nulo).
     * @param fragment Instancia del fragmento que se cargará.
     * @param containerId ID del contenedor donde se colocará el fragmento.
     * @throws IllegalStateException Si no se puede obtener un FragmentManager válido.
     */
    fun loadFragmentInstance(
        activity: AppCompatActivity?,
        thisFr: Fragment?,
        fragment: Fragment, containerId: Int
    ) {
        // Tomando el FragmentManager para manejar las transacciones de fragmentos
        val fragmentManager = configFrManager(activity, thisFr)
        check(fragmentManager != null) { "Invalid use of FragmentNavigator.loadFragmentInstance()" }

        fragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Configura y devuelve una instancia de FragmentManager basada en la actividad
     * y el fragmento actual proporcionados.
     *
     * @param activity Actividad que aloja la transacción del fragmento (puede ser nulo).
     * @param thisFr Fragmento actual (puede ser nulo).
     * @return Instancia de FragmentManager para manejar transacciones de fragmentos, o null si no es válido.
     */
    private fun configFrManager(
        activity: AppCompatActivity?, thisFr: Fragment?
    ): FragmentManager? {
        var fragmentManager: FragmentManager? = null
        // -If activity is not null and thisFr is null, use the activity's FragmentManager-
        if (activity != null && thisFr == null) {
            fragmentManager = activity.supportFragmentManager
        }
        // -If thisFr is not null and activity is null, use the fragment's parent FragmentManager-
        if (thisFr != null && activity == null) {
            fragmentManager = thisFr.parentFragmentManager
        }
        return fragmentManager
    }
}
