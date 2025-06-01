package com.mgsanlet.cheftube.domain.util.error

/**
 * Clase sellada que representa los posibles errores relacionados con usuarios.
 * Hereda de [DomainError] para integrarse con el sistema de manejo de errores del dominio.
 */
sealed class UserError: DomainError {
    /** Error que indica que el nombre de usuario ya está en uso. */
    data object UsernameInUse: UserError()
    
    /** Error que indica que el correo electrónico ya está registrado. */
    data object EmailInUse: UserError()
    
    /** Error que indica que el usuario no fue encontrado. */
    data object UserNotFound: UserError()
    
    /** Error que indica que las credenciales proporcionadas son incorrectas. */
    data object WrongCredentials: UserError()
    
    /** Error que indica que la contraseña es demasiado corta. */
    data object PasswordTooShort: UserError()
    
    /** Error que indica que el patrón de la contraseña no es válido. */
    data object InvalidPasswordPattern: UserError()
    
    /** Error que indica que el formato del correo electrónico no es válido. */
    data object InvalidEmailPattern: UserError()
    
    /** Error que indica que el formato del nombre de usuario no es válido. */
    data object InvalidUsernamePattern: UserError()
    
    /**
     * Error genérico para errores inesperados.
     *
     * @property messageArg Mensaje opcional que puede contener detalles adicionales sobre el error.
     */
    data class Unknown(val messageArg: Any? = ""): UserError()
}