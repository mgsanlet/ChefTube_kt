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

@AndroidEntryPoint
class ProfileFragment @Inject constructor() : BaseFragment<FragmentProfileBinding>() {

    private lateinit var sharedViewModel: ProfileViewModel
    private var isToggleInitialization: Boolean = true

    override fun onResume() {
        super.onResume()
        // Recargar los datos del usuario cuando se vuelva al fragmento
        isToggleInitialization = true
        // Actualizar los datos del usuario
        arguments?.let {
            it.getString(ARG_USER_ID)?.let {
                sharedViewModel.loadUserDataById(it)
            } ?: sharedViewModel.loadCurrentUserData()
        } ?: sharedViewModel.loadCurrentUserData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            it.getString(ARG_USER_ID)?.let {
                sharedViewModel.loadUserDataById(it)
            } ?: sharedViewModel.loadCurrentUserData()
        } ?: sharedViewModel.loadCurrentUserData()
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false)

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