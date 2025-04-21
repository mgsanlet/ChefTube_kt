package com.mgsanlet.cheftube.ui.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

/**
 * Utility class to manage fragment transactions in an AppCompatActivity.
 * Provides methods for loading fragments into specified containers.
 *
 * @author MarioG
 */
object FragmentNavigator {
    /**
     * Replaces the current fragment with a new fragment in the specified container.
     * The new fragment will be added to the back stack.
     * Should be used when no arguments are needed
     *
     * @param activity    The activity hosting the fragment transaction (can be null).
     * @param thisFr      The current fragment (can be null).
     * @param fragment    The fragment to be loaded.
     * @param containerId The ID of the container in which the fragment will be placed.
     */
    fun loadFragment(
        activity: AppCompatActivity?,
        thisFr: Fragment?,
        fragment: Fragment,
        containerId: Int
    ) {
        // -Getting the FragmentManager for handling fragment transactions-
        val fragmentManager = configFrManager(activity, thisFr)
        check(fragmentManager != null) { "Bad use of FragmentNavigator.loadFragment()" }

        fragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(containerId, fragment.javaClass, null)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Replaces the current fragment with a new instance of the specified fragment
     * in the given container. The fragment is added to the back stack.
     * Should be used when arguments are needed
     *
     * @param activity    The activity hosting the fragment transaction (can be null).
     * @param thisFr      The current fragment (can be null).
     * @param fragment    The fragment to be loaded.
     * @param containerId The ID of the container where the new fragment will be placed.
     */
    fun loadFragmentInstance(
        activity: AppCompatActivity?,
        thisFr: Fragment?,
        fragment: Fragment, containerId: Int
    ) {
        // -Getting the FragmentManager for handling fragment transactions-
        val fragmentManager = configFrManager(activity, thisFr)
        check(fragmentManager != null) { "Invalid use of FragmentNavigator.loadFragmentInstance()" }

        fragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Configures and returns a FragmentManager instance based on the provided
     * activity and current fragment.
     *
     * @param activity The activity hosting the fragment transaction (can be null).
     * @param thisFr   The current fragment (can be null).
     * @return The FragmentManager instance for handling fragment transactions, or null if invalid.
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
