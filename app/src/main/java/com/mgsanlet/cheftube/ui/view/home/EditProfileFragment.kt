package com.mgsanlet.cheftube.ui.view.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentEditProfileBinding
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.loadBitmapToCircle
import com.mgsanlet.cheftube.ui.util.loadUrlToCircle
import com.mgsanlet.cheftube.ui.util.setCustomStyle
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * ProfileFragment permite al usuario ver y modificar los detalles de su perfil,
 * incluyendo nombre de usuario, email y contraseña.
 */
@AndroidEntryPoint
class EditProfileFragment @Inject constructor() : BaseFragment<FragmentEditProfileBinding>() {

    private lateinit var sharedViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedViewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentEditProfileBinding = FragmentEditProfileBinding.inflate(inflater, container, false)

    override fun setUpObservers() {
        sharedViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProfileState.Error -> {
                    val errorMessage = state.error.asMessage(requireContext())
                    showLoading(false)
                    when (state.error) {

                        is UserError.UsernameInUse -> binding.usernameEditText.error = errorMessage
                        is UserError.InvalidUsernamePattern -> binding.usernameEditText.error =
                            errorMessage

                        else -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                is ProfileState.Loading -> showLoading(true)
                is ProfileState.LoadSuccess -> {
                    showLoading(false)
                    showUserData()
                }

                is ProfileState.SaveSuccess -> {
                    Toast.makeText(activity, getString(R.string.data_saved), Toast.LENGTH_SHORT)
                        .show()
                    FragmentNavigator.loadFragment(
                        null, this,
                        ProfileFragment(), R.id.fragmentContainerView
                    )
                }
            }
        }
    }

    private val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { imageUri ->
                // Iniciar UCrop
                val destinationUri = Uri.fromFile(requireContext().cacheDir.resolve("cropped_image.jpg"))
                UCrop.of(imageUri, destinationUri)
                    .withAspectRatio(1f, 1f) // Para que sea circular
                    .withMaxResultSize(1080, 1080)
                    .withOptions(UCrop.Options().apply {
                        setCircleDimmedLayer(true) // Habilitar el recorte circular
                        setCropFrameColor(requireContext().getColor(R.color.primary_green))
                        setToolbarTitle(getString(R.string.crop_image))
                    })
                    .start(requireContext(), this)
            }
        }
    }

    override fun setUpListeners() {
        binding.saveButton.setOnClickListener {
            sharedViewModel.tryUpdateCurrentUserData(
                binding.usernameEditText.text.toString(),
                binding.bioEditText.text.toString()
            )
        }
        binding.cancelButton.setOnClickListener {
            FragmentNavigator.loadFragment(
                null, this,
                ProfileFragment(), R.id.fragmentContainerView
            )
        }
        binding.profilePictureImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imagePicker.launch(intent)
        }
    }

    override fun setUpViewProperties() {
        binding.progressBar.setCustomStyle(requireContext())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            data?.let { intent ->
                UCrop.getOutput(intent)?.let { uri ->
                    // Convertir la imagen recortada a ByteArray
                    val bitmap = BitmapFactory.decodeFile(uri.path)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()

                    // Guardar la imagen en el ViewModel
                    sharedViewModel.setNewProfilePicture(byteArray)
                    
                    // Actualizar la vista previa
                    binding.profilePictureImageView.loadBitmapToCircle(bitmap, requireContext())
                }
            }
        }
    }

    private fun showUserData() {
        sharedViewModel.userData.value?.let { user ->
            binding.usernameEditText.setText(user.username)
            binding.bioEditText.setText(user.bio)
            
            // Cargar imagen de perfil
            if (user.profilePictureUrl.isNotEmpty()) {
                binding.profilePictureImageView.loadUrlToCircle(user.profilePictureUrl, requireContext())
            }
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
}