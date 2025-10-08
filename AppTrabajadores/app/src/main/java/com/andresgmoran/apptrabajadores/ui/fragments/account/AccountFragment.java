package com.andresgmoran.apptrabajadores.ui.fragments.account;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.models.User;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;

public class AccountFragment extends Fragment {
    private User actualUser = AppDataRepository.getInstance().getActualUser();
    private Bitmap userImage = AppDataRepository.getInstance().getActualUserImage();

    private View userCardView;
    private TextView userNameTextView;
    private ImageView userImageView;
    private Button residenceButton;
    private Button languageButton;
    private Button logOutButton;
    private Button toggleChangePasswordButton;
    private Button confirmChangePasswordButton;
    private LinearLayout passwordChangeLayout;
    private EditText etCurrentPassword, etNewPassword;

    private IOAccountFragmentListener accountFragmentListener;

    public interface IOAccountFragmentListener {
        void OnResidenceButtonClicked();
        void onLanguageSelected(String selectedLanguageCode);
        void onLogOutButtonClicked();
        void onChangePassword(String currentPassword, String newPassword);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View statusBar = requireActivity().findViewById(R.id.status_bar_background);
        statusBar.setBackgroundColor(Color.parseColor("#0062FF"));

        userCardView = view.findViewById(R.id.account_card_view);
        userCardView.findViewById(R.id.back_button).setVisibility(View.GONE);

        userNameTextView = userCardView.findViewById(R.id.banner_name_game);
        userNameTextView.setText(actualUser.getName() + " " + actualUser.getSurnames());

        userImageView = userCardView.findViewById(R.id.image_item_person_banner);
        userImageView.setImageBitmap(userImage);

        residenceButton = view.findViewById(R.id.residence_button_account);
        residenceButton.setOnClickListener(v -> accountFragmentListener.OnResidenceButtonClicked());

        languageButton = view.findViewById(R.id.btn_idioma);
        languageButton.setOnClickListener(v -> {
            final String[] idiomas = {
                    getString(R.string.es_language_text),
                    getString(R.string.en_language_text),
                    getString(R.string.va_language_text)
            };
            final String[] codigos = {"es", "en", "ca"};

            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.select_language_tile))
                    .setItems(idiomas, (dialog, which) -> {
                        String selectedLanguageCode = codigos[which];
                        accountFragmentListener.onLanguageSelected(selectedLanguageCode);
                    })
                    .show();
        });

        // Botón cerrar sesión
        logOutButton = view.findViewById(R.id.btn_cerrar_sesion);
        logOutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.logout_title))
                    .setMessage(getString(R.string.confirm_logout_message))
                    .setPositiveButton(getString(R.string.accept_text), (dialog, which) -> {
                        accountFragmentListener.onLogOutButtonClicked();
                    })
                    .setNegativeButton(getString(R.string.cancel_text), (dialog, which) -> dialog.dismiss())
                    .show();
        });

        toggleChangePasswordButton = view.findViewById(R.id.btn_toggle_password_change);
        confirmChangePasswordButton = view.findViewById(R.id.btn_change_password);
        passwordChangeLayout = view.findViewById(R.id.password_change_container);
        etCurrentPassword = view.findViewById(R.id.et_current_password);
        etNewPassword = view.findViewById(R.id.et_new_password);

        toggleChangePasswordButton.setOnClickListener(v -> {
            if (passwordChangeLayout.getVisibility() == View.VISIBLE) {
                passwordChangeLayout.setVisibility(View.GONE);
            } else {
                passwordChangeLayout.setVisibility(View.VISIBLE);
            }
        });

        confirmChangePasswordButton.setOnClickListener(v -> {
            String currentPass = etCurrentPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();

            if (currentPass.isEmpty()) {
                etCurrentPassword.setError("Campo obligatorio");
            } else if (newPass.isEmpty()) {
                etNewPassword.setError("Campo obligatorio");
            } else {
                etCurrentPassword.setError(null);
                etNewPassword.setError(null);
                accountFragmentListener.onChangePassword(currentPass, newPass);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        accountFragmentListener = (IOAccountFragmentListener) context;
    }
}
