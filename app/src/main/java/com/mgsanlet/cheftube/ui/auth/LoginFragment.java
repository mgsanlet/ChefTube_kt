package com.mgsanlet.cheftube.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.mgsanlet.cheftube.data.model.User;
import com.mgsanlet.cheftube.data.local.UserDAO;

import com.mgsanlet.cheftube.utils.FragmentNavigator;
import com.mgsanlet.cheftube.ui.home.HomeActivity;
import com.mgsanlet.cheftube.R;

/**
 * A fragment that handles the login process for the application.
 * It allows users to enter their credentials (username and password) and
 * logs them into the application. Additionally, it provides a link to navigate
 * to the sign-up fragment if the user does not have an account.
 *
 * @author MarioG
 */
public class LoginFragment extends Fragment {
    // -Declaring constants for argument keys-
    private static final String ARG_USER = "user";

    // -Declaring data members-
    private User mUser;
    // -Declaring UI elements-
    EditText identityField;
    EditText pwdField;
    Button loginBtn;
    TextView signUpLink;
    // -Declaring intent-
    Intent mainActIntent;
    // -Declaring string resources-
    String requiredStr;
    String invalidLoginStr;

    /**
     * Creates a new instance of LoginFragment with the specified user credentials.
     * This method is used from the SignUpFragment to prefill the login form with
     * the user's credentials.
     *
     * @param user The {@link User} object containing the username and password to be prefilled.
     * @return A new instance of LoginFragment with the provided user credentials.
     */
    public static LoginFragment newInstance(User user) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // -Getting arguments passed to the fragment-
        if (getArguments() != null) {
            mUser = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // -Initializing UI elements-
        identityField = view.findViewById(R.id.loginIdentityField);
        pwdField = view.findViewById(R.id.loginPwdField);
        loginBtn = view.findViewById(R.id.signInBtn);
        signUpLink = view.findViewById(R.id.loginSignUpLink);
        // -Initializing intent-
        mainActIntent = new Intent(getActivity(), HomeActivity.class);
        // -Initializing string resources-
        requiredStr = getString(R.string.required);
        invalidLoginStr = getString(R.string.invalid_login);

        // -Setting username and password if passed from the previous screen-
        if (mUser != null) {
            identityField.setText(mUser.getUsername());
            pwdField.setText(mUser.getPassword());
        }

        // -Setting up listeners-
        loginBtn.setOnClickListener(v -> tryLogin());

        signUpLink.setOnClickListener(v -> {
            cleanErrors();
            FragmentNavigator.loadFragment(null, this, new SignUpFragment(), R.id.authFrContainer);
        });
        Log.i("dbtest", "Starting authentication activity...");
        UserDAO.logDBContent(getContext());
        return view;
    }

    /**
     * Attempts to log the user in by validating their input and starting the main activity
     * if the credentials are correct. Displays a toast message if the login fails.
     */
    private void tryLogin() {
        if (fieldsAreEmpty()) return;

        // -Getting the valid user if the credentials match-
        User validUser = UserDAO.getValidUser(
                identityField.getText().toString(),
                pwdField.getText().toString(),
                getContext()
        );

        if (validUser == null) {
            Toast.makeText(getContext(), invalidLoginStr, Toast.LENGTH_SHORT).show();
        } else {
            navToHomePage(validUser);
        }
    }

    /**
     * Checks whether the login fields (username and password) are empty.
     * If any field is empty, an error message is displayed for the corresponding field.
     *
     * @return True if any field is empty, false otherwise.
     */
    private boolean fieldsAreEmpty() {
        boolean empty = false;
        if (identityField.getText().toString().trim().isEmpty()) {
            identityField.setError(requiredStr);
            empty = true;
        }
        if (pwdField.getText().toString().trim().isEmpty()) {
            pwdField.setError(requiredStr);
            empty = true;
        }
        return empty;
    }

    /**
     * Clears the error messages from the input fields (username and password).
     */
    public void cleanErrors() {
        identityField.setError(null);
        pwdField.setError(null);
    }

    /**
     * Navigates to the home page (HomeActivity) with the valid user's data.
     *
     * @param validUser  The {@link User} object representing the user that successfully logged in.
     */
    private void navToHomePage(User validUser ) {
        Log.i("dbtest", "Logging in with id: " + validUser .getId());
        mainActIntent.putExtra("user", validUser );
        startActivity(mainActIntent);
        if (getActivity() != null) getActivity().finish();
    }
}