package com.mgsanlet.cheftube.ui.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentProfileBinding
import com.mgsanlet.cheftube.ui.util.Constants.ARG_USER_ID
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.loadUrlToCircle
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragmento que muestra el perfil de un usuario.
 *
 * Este fragmento es responsable de:
 * - Mostrar la información del perfil del usuario (foto, nombre, biografía)
 * - Gestionar la funcionalidad de seguir/dejar de seguir
 * - Navegar a la edición del perfil (solo para el perfil propio)
 * - Mostrar las recetas del usuario
 *
 * @constructor Crea una nueva instancia del fragmento de perfil
 */
@AndroidEntryPoint
class ProfileFragment @Inject constructor() : BaseFragment<FragmentProfileBinding>() {

    /** ViewModel compartido para manejar la lógica del perfil */
    private lateinit var sharedViewModel: ProfileViewModel
    
    /** Bandera para controlar la inicialización del toggle de seguimiento */
    private var isToggleInitialization: Boolean = true

    /**
     * Se llama cuando el fragmento es visible para el usuario y está en primer plano.
     * 
     * Recarga los datos del perfil cuando el usuario vuelve a este fragmento.
     */
    override fun onResume() {
        super.onResume()
        // Recargar los datos del usuario cuando se vuelva al fragmento
        isToggleInitialization = true
        // Actualizar los datos del usuario
        arguments?.let {
            it.getString(ARG_USER_ID)?.let { userId ->
                sharedViewModel.loadUserDataById(userId)
            } ?: sharedViewModel.loadCurrentUserData()
        } ?: sharedViewModel.loadCurrentUserData()
    }

    /**
     * Se llama para crear la jerarquía de vistas asociada con el fragmento.
     * 
     * Inicializa el ViewModel compartido para mantener los datos del perfil.
     *
     * @param inflater El LayoutInflater usado para inflar la vista
     * @param container El ViewGroup padre al que se adjuntará la vista
     * @param savedInstanceState Estado previamente guardado de la instancia
     * @return La vista inflada para el fragmento
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    /**
     * Se llama después de que la vista ha sido creada.
     * 
     * Carga los datos del perfil según el ID de usuario proporcionado en los argumentos.
     * Si no se proporciona un ID, carga el perfil del usuario actual.
     *
     * @param view La vista devuelta por onCreateView
     * @param savedInstanceState Estado previamente guardado de la instancia
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            bundle.getString(ARG_USER_ID)?.let { userId ->
                sharedViewModel.loadUserDataById(userId)
            } ?: sharedViewModel.loadCurrentUserData()
        } ?: sharedViewModel.loadCurrentUserData()
    }

    /**
     * Infla y devuelve el binding para el layout del fragmento.
     *
     * @param inflater El LayoutInflater usado para inflar la vista
     * @param container El ViewGroup padre al que se adjuntará la vista
     * @return Instancia del binding inflado
     */
    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

    /**
     * Configura los observadores para los estados del ViewModel.
     * 
     * Maneja los diferentes estados de la UI:
     * - Error: Muestra mensajes de error
     * - LoadSuccess: Muestra los datos del perfil cuando se cargan correctamente
     * - isCurrentUserProfile: Actualiza la interfaz según si es el perfil propio o de otro usuario
     */
    override fun setUpObservers() {
        sharedViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Error -> {
                    val errorMessage = state.error.asMessage(requireContext())
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }

                is ProfileState.LoadSuccess -> {
                    showUserData()
                }

                else -> {}
            }
        }

        sharedViewModel.isCurrentUserProfile.observe(viewLifecycleOwner) { isCurrent ->
            when (isCurrent) {
                true -> {
                    binding.editButton.visibility = View.VISIBLE
                    binding.followToggle.visibility = View.INVISIBLE
                    binding.createRecipeButton.visibility = View.VISIBLE
                    binding.addIcon.visibility = View.VISIBLE
                }

                false -> {
                    binding.editButton.visibility = View.INVISIBLE
                    binding.followToggle.visibility = View.VISIBLE
                    binding.createRecipeButton.visibility = View.INVISIBLE
                    binding.addIcon.visibility = View.INVISIBLE
                }
            }
        }
    }

    /**
     * Configura los listeners para los elementos de la interfaz de usuario.
     * 
     * Maneja las siguientes interacciones:
     * - Botón de editar perfil (solo visible para el perfil propio)
     * - Toggle de seguir/dejar de seguir (solo visible para perfiles ajenos)
     * - Botones para ver recetas creadas y favoritas
     * - Botón para crear una nueva receta (solo para el perfil propio)
     */
    override fun setUpListeners() {
        binding.editButton.setOnClickListener {
            FragmentNavigator.loadFragment(
                null, this,
                EditProfileFragment(), R.id.fragmentContainerView
            )
        }
        binding.followToggle.setOnCheckedChangeListener { _, isChecked ->
            if (!isToggleInitialization) {
                sharedViewModel.followUser(isChecked)
                var currentFollowers = binding.followersTextView.text.toString().substringBefore(" ").toInt()
                binding.followersTextView.text = getString(
                    R.string.followers,
                    currentFollowers + if (isChecked) 1 else -1
                )
            }
        }

        binding.seeCreatedButton.setOnClickListener {
            val createdRecipes = sharedViewModel.getProfileUserCreatedRecipes()
            if (createdRecipes.isEmpty()) {
                Toast.makeText(context, getString(R.string.no_recipes_created), Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            val recipeFeedFragment =
                RecipeFeedFragment.newInstance(createdRecipes as ArrayList<String>)
            FragmentNavigator.loadFragmentInstance(
                null, this,
                recipeFeedFragment, R.id.fragmentContainerView
            )
        }
        binding.seeFavButton.setOnClickListener {
            val favouriteRecipes = sharedViewModel.getProfileUserFavouriteRecipes()
            if (favouriteRecipes.isEmpty()) {
                Toast.makeText(context, getString(R.string.no_favourite_recipes), Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            val recipeFeedFragment =
                RecipeFeedFragment.newInstance(favouriteRecipes as ArrayList<String>)
            FragmentNavigator.loadFragmentInstance(
                null, this,
                recipeFeedFragment, R.id.fragmentContainerView
            )
        }

        binding.createRecipeButton.setOnClickListener {
            FragmentNavigator.loadFragment(
                null, this,
                RecipeFormFragment(), R.id.fragmentContainerView
            )
        }
    }

    /**
     * Muestra los datos del perfil del usuario en la interfaz.
     * 
     * Actualiza la imagen de perfil, nombre, biografía y otros detalles del perfil.
     * También configura los listeners para los botones de seguir/editar.
     */
    private fun showUserData() {
        sharedViewModel.userData.value?.let { user ->
            binding.usernameTextView.text = user.username
            binding.emailTextView.text = user.email
            binding.followToggle.isChecked = sharedViewModel.isUserBeingFollowed()
            isToggleInitialization = false
            binding.followersTextView.text = getString(R.string.followers, user.followersIds.size)
            binding.followingTextView.text = getString(R.string.following, user.followingIds.size)
            binding.bioTextView.text = user.bio
            binding.seeCreatedButton.text = getString(R.string.see_created_recipes, user.username)
            binding.seeFavButton.text = getString(R.string.see_favourite_recipes, user.username)

            // Cargar imagen de perfil
            if (user.profilePictureUrl.isNotBlank()) {
                binding.profilePictureImageView.loadUrlToCircle(
                    user.profilePictureUrl,
                    requireContext()
                )
            } else {
                binding.profilePictureImageView.setImageResource(R.drawable.ic_default_avatar_24)
            }
        }
    }

    companion object {

        fun newInstance(userId: String? = null): ProfileFragment {
            return ProfileFragment().apply {
                arguments = Bundle().apply {
                    userId?.let { putString(ARG_USER_ID, it) }
                }
            }
        }
    }
}