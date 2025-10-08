package com.andresgmoran.apptrabajadores.ui.fragments.authentication;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.models.User;

import java.util.List;

public class LoginFragment extends Fragment {

    private OnLoginListener loginListener;

    public interface OnLoginListener {
        void onLogin(String email, String password, boolean rememberPassword);
    }

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private CheckBox rememberPasswordCheckBox;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailInput = view.findViewById(R.id.email_input_login);
        passwordInput = view.findViewById(R.id.password_input_login);
        rememberPasswordCheckBox = view.findViewById(R.id.remember_password_checkBox);
        loginButton = view.findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getActivity(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginListener.onLogin(email, password, rememberPasswordCheckBox.isChecked());
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginListener) {
            loginListener = (OnLoginListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnLoginListener");
        }
    }
}

