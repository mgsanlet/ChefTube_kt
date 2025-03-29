package com.mgsanlet.cheftube.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import model.User;
import model.UserDAO;

import com.mgsanlet.cheftube.FragmentNavigator;
import com.mgsanlet.cheftube.R;

/**
 * A fragment responsible for handling the user sign-up process.
 * This fragment allows the user to register by providing a name, email, and password.
 * It validates the input data and ensures that the email is not already in use.
 * After successful registration, the user is redirected to the login
 * fragment with credentials prefilled.
 *
 * @author MarioG
 */
public class SignUpFragment extends Fragment {
    // -Declaring UI elements-
    EditText nameField;
    EditText emailField;
    EditText pwdField;
    EditText pwd2Field;
    Button saveBtn;
    // -Declaring string resources-
    String requiredStr;
    String invalidEmailStr;
    String emailAlreadyStr;
    String usernameAlreadyStr;
    String shortPwdStr;
    String pwdDMatchStr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        // -Initializing UI elements-
        nameField = view.findViewById(R.id.signUpNameField);
        emailField = view.findViewById(R.id.signUpEmailField);
        pwdField = view.findViewById(R.id.signUpPwdField);
        pwd2Field = view.findViewById(R.id.signUpPwd2Field);
        saveBtn = view.findViewById(R.id.saveBtn);
        // -Initializing string resources-
        requiredStr = getString(R.string.required);
        invalidEmailStr = getString(R.string.invalid_email);
        emailAlreadyStr = getString(R.string.email_already);
        usernameAlreadyStr = getString(R.string.username_already);
        shortPwdStr = getString(R.string.short_pwd);
        pwdDMatchStr = getString(R.string.pwd_d_match);

        // -Setting up listeners-
        saveBtn.setOnClickListener(view1 -> {
            if (isValidRegister()) {
                loadLoginFr();
            }
        });

        return view;
    }

    /**
     * Validates the user input for registration. It ensures all fields are filled,
     * the email format is correct, the email is not already in use, the password is valid,
     * and the password fields match.
     *
     * @return True if all validation checks pass, false otherwise.
     */
    private boolean isValidRegister() {
        return (!fieldsAreEmpty() &&
                isValidEmail() &&
                !isExistentEmail() &&
                !isExistentUsername() &&
                isValidPwd() &&
                pwdsMatch()
        );
    }

    /**
     * Checks if any of the required fields (name, email, password) are empty.
     * Sets an error message on the corresponding field if it is empty.
     *
     * @return True if any field is empty, false otherwise.
     */
    private boolean fieldsAreEmpty() {
        boolean empty = false;
        if (nameField.getText().toString().trim().isEmpty()) {
            nameField.setError(requiredStr);
            empty = true;
        }
        if (emailField.getText().toString().trim().isEmpty()) {
            emailField.setError(requiredStr);
            empty = true;
        }
        if (pwdField.getText().toString().trim().isEmpty()) {
            pwdField.setError(requiredStr);
            empty = true;
        }
        if (pwd2Field.getText().toString().trim().isEmpty()) {
            pwd2Field.setError(requiredStr);
            empty = true;
        }
        return empty;
    }

    /**
     * Validates that the email entered is in a valid email format using regular expressions.
     *
     * @return True if the email is valid, false otherwise.
     */
    private boolean isValidEmail() {
        String email = emailField.getText().toString();
        boolean isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        if (!isValid) {
            emailField.setError(invalidEmailStr);
        }
        return isValid;
    }

    /**
     * Checks if the entered email already exists in the system by comparing it with
     * the emails of all registered users.
     *
     * @return True if the email already exists, false otherwise.
     */
    private boolean isExistentEmail() {
        String inputEmail = emailField.getText().toString();

        boolean isExistent = UserDAO.isExistentEmail(inputEmail, getContext());

        if (isExistent) {
            emailField.setError(emailAlreadyStr);
        }
        return isExistent;
    }

    /**
     * Checks if the entered username already exists in the system by comparing it with
     * the usernames of all registered users.
     *
     * @return True if the username already exists, false otherwise.
     */
    private boolean isExistentUsername() {

        String inputUsername = nameField.getText().toString();

        boolean isExistent = UserDAO.isExistentUsername(inputUsername, getContext());

        if (isExistent) {
            nameField.setError(usernameAlreadyStr);
        }
        return isExistent;
    }

    /**
     * Validates the password to ensure it is at least 5 characters long.
     *
     * @return True if the password is valid, false otherwise.
     */
    private boolean isValidPwd() {
        boolean isValid = true;
        if (pwdField.getText().toString().length() < 5) {
            pwdField.setError(shortPwdStr);
            isValid = false;
        }
        return isValid;
    }

    /**
     * Checks if the two password fields match.
     *
     * @return True if the passwords match, false otherwise.
     */
    private boolean pwdsMatch() {
        boolean areMatching = false;
        if (pwdField.getText().toString().equals(pwd2Field.getText().toString())) {
            areMatching = true;
        } else {
            pwd2Field.setError(pwdDMatchStr);
        }
        return areMatching;
    }

    /**
     * Loads the LoginFragment after successful registration.
     * The new user is registered in the UserDAO and the user is redirected to the login page
     * with prefilled credentials.
     */
    private void loadLoginFr() {
        cleanErrors();
        User newUser = getNewUser();
        UserDAO.register(newUser, getContext());
        LoginFragment loginFr = LoginFragment.newInstance(newUser);
        FragmentNavigator.loadFragmentInstance(null, this, loginFr, R.id.authFrContainer);
    }

    /**
     * Clears all error messages from the input fields (name, email, password).
     */
    private void cleanErrors() {
        nameField.setError(null);
        emailField.setError(null);
        pwdField.setError(null);
        pwd2Field.setError(null);
    }

    /**
     * Creates a new user object with the provided name, email, and password fields.
     *
     * @return A new User object containing the registration details.
     */
    private User getNewUser() {
        return new User(nameField.getText().toString(),
                emailField.getText().toString(),
                pwdField.getText().toString());
    }
}