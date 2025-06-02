package com.mgsanlet.cheftube.ui.view.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.mgsanlet.cheftube.R
import com.mgsanlet.cheftube.databinding.FragmentEditProfileBinding
import com.mgsanlet.cheftube.domain.util.error.UserError
import com.mgsanlet.cheftube.ui.util.FragmentNavigator
import com.mgsanlet.cheftube.ui.util.asMessage
import com.mgsanlet.cheftube.ui.util.loadBitmapToCircle
import com.mgsanlet.cheftube.ui.util.loadUrlToCircle
import com.mgsanlet.cheftube.ui.view.auth.AuthActivity
import com.mgsanlet.cheftube.ui.view.base.BaseFragment
import com.mgsanlet.cheftube.ui.view.dialogs.DeleteAccountDialog
import com.mgsanlet.cheftube.ui.view.dialogs.LoadingDialog
import com.mgsanlet.cheftube.ui.view.dialogs.UpdatePasswordDialog
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileState
import com.mgsanlet.cheftube.ui.viewmodel.home.ProfileViewModel
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * Fragmento que permite al usuario editar su perfil y configuraciones de cuenta.
 *
 * Este fragmento es responsable de:
 * - Mostrar y permitir la edición de la información del perfil (nombre de usuario, biografía, foto)
 * - Gestionar el cambio de contraseña
 * - Manejar la eliminación de la cuenta
 * - Procesar la imagen de perfil (selección y recorte)
 *
 * @constructor Crea una nueva instancia del fragmento de edición de perfil
 */
@AndroidEntryPoint
class EditProfileFragment @Inject constructor() : BaseFragment<FragmentEditProfileBinding>() {

    /** ViewModel compartido para manejar la lógica del perfil */
    private lateinit var sharedViewModel: ProfileViewModel

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
     * Infla y devuelve el binding para el layout del fragmento.
     *
     * @param inflater El LayoutInflater usado para inflar la vista
     * @param container El ViewGroup padre al que se adjuntará la vista
     * @return Instancia del binding inflado
     */
    override fun inflateViewBinding(
        inflater: LayoutInflater, container: ViewGroup?
    ): FragmentEditProfileBinding = FragmentEditProfileBinding.inflate(inflater, container, false)

    /**
     * Configura los observadores para los estados del ViewModel.
     * 
     * Maneja los diferentes estados de la UI:
     * - Error: Muestra mensajes de error específicos para cada campo
     * - Loading: Muestra u oculta el indicador de carga
     * - LoadSuccess: Muestra los datos actuales del perfil
     * - SaveSuccess: Navega de vuelta al perfil tras guardar los cambios
     * - PasswordUpdated: Muestra confirmación de cambio de contraseña
     * - AccountDeleted: Navega a la pantalla de autenticación
     */
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
                        // Estos errores los manejan los diálogos de gestión de cuenta
                        is UserError.WrongCredentials -> {}
                        is UserError.PasswordTooShort -> {}
                        is UserError.InvalidPasswordPattern -> {}

                        else -> Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                is ProfileState.Loading -> showLoading(true)
                is ProfileState.LoadSuccess -> {
                    showLoading(false)
                    showUserData()
                }

                is ProfileState.SaveSuccess -> {
                    showLoading(false)
                    Toast.makeText(activity, getString(R.string.data_saved), Toast.LENGTH_SHORT)
                        .show()
                    FragmentNavigator.loadFragment(
                        null, this,
                        ProfileFragment(), R.id.fragmentContainerView
                    )
                }

                is ProfileState.PasswordUpdated -> {
                    showToast(R.string.password_updated_successfully)
                    showLoading(false)
                }
                is ProfileState.AccountDeleted -> {
                    // Navegar a la pantalla de inicio de sesión
                    startActivity(Intent(requireContext(), AuthActivity::class.java))
                    requireActivity().finish()
                }

                else -> { showLoading(false) }
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

    /**
     * Configura los listeners para los elementos de la interfaz de usuario.
     * 
     * Incluye:
     * - Botón de guardar cambios
     * - Botón de cambio de contraseña
     * - Botón de eliminar cuenta
     * - Botón para seleccionar/cambiar foto de perfil
     */
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

        binding.accountSettingsView.setOnPasswordClickListener {
            val dialog = UpdatePasswordDialog(this)
            dialog.setOnPasswordUpdatedListener {
                // Mostrar mensaje de éxito
                showToast(R.string.password_updated_successfully)
            }
            dialog.show(parentFragmentManager, UpdatePasswordDialog.TAG)
        }

        binding.accountSettingsView.setOnDeleteClickListener {
            val dialog = DeleteAccountDialog(this)
            dialog.setOnAccountDeletedListener {
                // La navegación se maneja en el observer del estado AccountDeleted
            }
            dialog.show(parentFragmentManager, DeleteAccountDialog.TAG)
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
            binding.accountSettingsView.setOnExpandDownClickListener {
                scrollToAccountSettings()
            }
        }
    }

    /**
     * Desplaza la vista hasta la sección de configuración de cuenta.
     * 
     * Este método se utiliza para asegurar que la sección de configuración de cuenta
     * sea visible cuando el usuario interactúa con elementos de la interfaz que
     * requieren desplazamiento, especialmente en dispositivos con Android Q o superior.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun scrollToAccountSettings() {
        binding.scrollView.scrollToDescendant(binding.accountSettingsView)
    }

    /**
     * Maneja el resultado de las actividades iniciadas por este fragmento.
     * 
     * En particular, procesa el resultado del recorte de imagen de perfil utilizando UCrop.
     * 
     * @param requestCode Código de solicitud que identifica de qué actividad proviene el resultado
     * @param resultCode Código que indica el resultado de la operación
     * @param data Intent que contiene los datos resultantes
     */
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
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

    /**
     * Muestra los datos actuales del perfil en el formulario de edición.
     * 
     * Carga la imagen de perfil, nombre de usuario, email y biografía
     * en los campos correspondientes.
     */
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

    /**
     * Muestra u oculta el diálogo de carga.
     * 
     * @param show true para mostrar el diálogo de carga, false para ocultarlo
     */
    private fun showLoading(show: Boolean) {
        if (show) {
            LoadingDialog.show(requireContext(), parentFragmentManager)
        } else {
            LoadingDialog.dismiss(parentFragmentManager)
        }
    }

    /**
     * Se llama cuando la vista del fragmento está a punto de ser destruida.
     * 
     * Asegura que cualquier diálogo de carga activo sea descartado
     * para evitar memory leaks.
     */
    override fun onDestroyView() {
        LoadingDialog.dismiss(parentFragmentManager)
        super.onDestroyView()
    }
}