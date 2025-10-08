package com.andresgmoran.apptrabajadores.ui.fragments.admin;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.andresgmoran.apptrabajadores.R;
import com.andresgmoran.apptrabajadores.models.Game;
import com.andresgmoran.apptrabajadores.models.Residence;
import com.andresgmoran.apptrabajadores.models.Resident;
import com.andresgmoran.apptrabajadores.models.User;
import com.andresgmoran.apptrabajadores.repository.AppDataRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPanelFragment extends Fragment {

    private Map<String, View> actionViews = new HashMap<>();
    private List<Residence> residences = new ArrayList<>();
    private List<Resident> residents = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private List<Game> games = new ArrayList<>();

    private static final String[] ACTIONS = {
            "action_add_residencia", "action_delete_residencia",
            "action_add_user", "action_delete_user",
            "action_add_resident", "action_delete_resident",
            "action_add_game", "action_delete_game"
    };

    public interface IOnAdminPanel{
        void onLogOutButtonClicked();
    }
    private IOnAdminPanel listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        for (String idName : ACTIONS) {
            View actionLayout = root.findViewById(getResources().getIdentifier(idName, "id", requireContext().getPackageName()));
            if (actionLayout != null) {
                setupActionLayout(idName, actionLayout);
                actionViews.put(idName, actionLayout);
            }
        }
        Button logoutButton = root.findViewById(R.id.btn_logout_admin);
        logoutButton.setOnClickListener( v -> {
            if (listener != null) {
                listener.onLogOutButtonClicked();
            }
        });
    }

    private void setupActionLayout(String idName, View layout) {
        TextView title = layout.findViewById(R.id.title);
        LinearLayout container = layout.findViewById(R.id.container);
        Button executeBtn = layout.findViewById(R.id.btn_execute);
        Spinner spinnerResidencias = layout.findViewById(R.id.spinner_residencias);
        Spinner spinnerExtra = layout.findViewById(R.id.spinner_extra);

        EditText residenceName = layout.findViewById(R.id.residence_name_input);
        EditText residenceEmail = layout.findViewById(R.id.residence_email_input);

        title.setText(formatActionTitle(idName));
        title.setOnClickListener(v -> container.setVisibility(container.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE));

        spinnerResidencias.setVisibility(View.GONE);
        spinnerExtra.setVisibility(View.GONE);

        layout.findViewById(R.id.layout_residence_inputs).setVisibility(View.GONE);
        layout.findViewById(R.id.layout_resident_inputs).setVisibility(View.GONE);
        layout.findViewById(R.id.layout_user_inputs).setVisibility(View.GONE);
        layout.findViewById(R.id.layout_game_inputs).setVisibility(View.GONE);

        switch (idName) {
            case "action_add_residencia":
                layout.findViewById(R.id.layout_residence_inputs).setVisibility(View.VISIBLE);

                executeBtn.setOnClickListener(v -> {
                    String name = residenceName.getText().toString().trim();
                    String email = residenceEmail.getText().toString().trim();

                    boolean valid = true;

                    if (name.isEmpty()) {
                        residenceName.setError("Nombre obligatorio");
                        valid = false;
                    } else {
                        residenceName.setError(null);
                    }

                    if (email.isEmpty()) {
                        residenceEmail.setError("Email obligatorio");
                        valid = false;
                    } else {
                        residenceEmail.setError(null);
                    }

                    if (valid) {
                        addResidencia(name, email);
                    }
                });
                break;

            case "action_delete_residencia":
                setupResidenciaSpinner(spinnerResidencias);
                executeBtn.setOnClickListener(v -> {
                    int pos = spinnerResidencias.getSelectedItemPosition();
                    if (pos >= 0) deleteResidencia(residences.get(pos).getId());
                });
                break;
            case "action_add_user":
                layout.findViewById(R.id.layout_user_inputs).setVisibility(View.VISIBLE);
                layout.findViewById(R.id.layout_user_inputs).findViewById(R.id.et_add_user_verfication_code).setVisibility(View.GONE);
                setupResidenciaSpinner(spinnerResidencias);
                EditText userName = layout.findViewById(R.id.et_add_user_nombre);
                EditText userSurname = layout.findViewById(R.id.et_add_user_apellido);
                EditText userEmail = layout.findViewById(R.id.et_add_user_email);
                EditText userPassword = layout.findViewById(R.id.et_add_user_password);
                executeBtn.setOnClickListener( v -> {
                    String name = userName.getText().toString().trim();
                    String surname = userSurname.getText().toString().trim();
                    String email = userEmail.getText().toString().trim();
                    String password = userPassword.getText().toString().trim();

                    boolean valid = true;
                    if (userName.getText().toString().trim().isEmpty()) {
                        userName.setError("Nombre obligatorio");
                        valid = false;
                    } else {
                        userName.setError(null);
                    }

                    if (userSurname.getText().toString().trim().isEmpty()) {
                        userSurname.setError("Apellidos obligatorios");
                        valid = false;
                    } else {
                        userSurname.setError(null);
                    }

                    if (email.isEmpty()) {
                        userEmail.setError("Email obligatorio");
                        valid = false;
                    } else {
                        userEmail.setError(null);
                    }

                    if (password.isEmpty()) {
                        userPassword.setError("Contraseña obligatoria");
                        valid = false;
                    } else {
                        userPassword.setError(null);
                    }

                    if (valid) {
                        int pos = spinnerResidencias.getSelectedItemPosition();
                        if (pos >= 0) {
                            AppDataRepository.getInstance().addUser(requireContext(), residences.get(pos).getId(),name, surname,  email, password,
                                    () -> {
                                        Toast.makeText(requireContext(), "Usuario añadido", Toast.LENGTH_SHORT).show();
                                        refreshData();
                                        layout.findViewById(R.id.layout_user_inputs).findViewById(R.id.et_add_user_verfication_code).setVisibility(View.VISIBLE);
                                        EditText verificationCode = layout.findViewById(R.id.et_add_user_verfication_code);
                                        executeBtn.setOnClickListener( v2 -> {
                                            String code = verificationCode.getText().toString().trim();
                                            if (code.isEmpty()) {
                                                verificationCode.setError("Código de verificación obligatorio");
                                            } else {
                                                AppDataRepository.getInstance().verifyUser(requireContext(), email, code,
                                                        () -> Toast.makeText(requireContext(), "Usuario verificado", Toast.LENGTH_SHORT).show(),
                                                        () -> Toast.makeText(requireContext(), "Error al verificar usuario", Toast.LENGTH_SHORT).show());
                                            }
                                        });

                                    },
                                    () -> Toast.makeText(requireContext(), "Error al añadir usuario", Toast.LENGTH_SHORT).show());
                        }
                    }
                });
                break;

            case "action_delete_user":
                setupResidenciaSpinner(spinnerResidencias);
                spinnerResidencias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setupUserSpinner(spinnerExtra, residences.get(position).getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
                executeBtn.setOnClickListener(v -> {
                    int posResi = spinnerResidencias.getSelectedItemPosition();
                    int pos = spinnerExtra.getSelectedItemPosition();
                    if (pos >= 0 && pos < users.size() && posResi >= 0) {
                        List<User> filteredUsers = (List<User>) spinnerExtra.getTag();
                        deleteUser( residences.get(posResi).getId(), filteredUsers.get(pos).getId());
                    }
                });
                break;

            case "action_add_resident":
                setupResidenciaSpinner(spinnerResidencias);
                layout.findViewById(R.id.layout_resident_inputs).setVisibility(View.VISIBLE);

                EditText residentName = layout.findViewById(R.id.et_add_resident_nombre);
                EditText residentSurnames = layout.findViewById(R.id.et_add_resident_apellido);
                EditText residentFechaNac = layout.findViewById(R.id.et_add_resident_fecha_nacimiento);
                EditText residentIDDocument = layout.findViewById(R.id.et_add_resident_documento_identidad);
                EditText residentDocumentYear = layout.findViewById(R.id.et_add_resident_year);
                EditText residentDocumentMonth = layout.findViewById(R.id.et_add_resident_month);
                EditText residentFamily1 = layout.findViewById(R.id.et_add_resident_familiar_1);
                EditText residentFamily2 = layout.findViewById(R.id.et_add_resident_familiar_2);

                executeBtn.setOnClickListener(v -> {
                    String name = residentName.getText().toString().trim();
                    String surnames = residentSurnames.getText().toString().trim();
                    String fechaNac = residentFechaNac.getText().toString().trim();
                    String idDocument = residentIDDocument.getText().toString().trim();
                    String documentYear = residentDocumentYear.getText().toString().trim();
                    String documentMonth = residentDocumentMonth.getText().toString().trim();
                    String family1 = residentFamily1.getText().toString().trim();
                    String family2 = residentFamily2.getText().toString().trim();

                    boolean valid = true;

                        // Validación campos texto
                    if (name.isEmpty()) {
                        residentName.setError("Nombre obligatorio");
                        valid = false;
                    } else {
                        residentName.setError(null);
                    }

                    if (surnames.isEmpty()) {
                        residentSurnames.setError("Apellidos obligatorios");
                        valid = false;
                    } else {
                        residentSurnames.setError(null);
                    }

                    if (idDocument.isEmpty()) {
                        residentIDDocument.setError("Documento obligatorio");
                        valid = false;
                    } else {
                        residentIDDocument.setError(null);
                    }

                    if (family1.isEmpty()) {
                        residentFamily1.setError("Familiar 1 obligatorio");
                        valid = false;
                    } else {
                        residentFamily1.setError(null);
                    }
                    if (family2.isEmpty()) {
                        residentFamily2.setError("Familiar 2 obligatorio");
                        valid = false;
                    } else {
                        residentFamily2.setError(null);
                    }

                        // Validación fecha nacimiento
                    LocalDate fechaNacimiento = null;
                    try {
                        fechaNacimiento = LocalDate.parse(fechaNac); // formato: yyyy-MM-dd
                        residentFechaNac.setError(null);
                    } catch (Exception e) {
                        residentFechaNac.setError("Formato válido: yyyy-MM-dd");
                        valid = false;
                    }

                        // Validación año y mes
                    int year = -1, month = -1;

                    try {
                        year = Integer.parseInt(documentYear);
                        residentDocumentYear.setError(null);
                    } catch (NumberFormatException e) {
                        residentDocumentYear.setError("Debe ser un número");
                        valid = false;
                    }

                    try {
                        month = Integer.parseInt(documentMonth);
                        residentDocumentMonth.setError(null);
                    } catch (NumberFormatException e) {
                        residentDocumentMonth.setError("Debe ser un número");
                        valid = false;
                    }

                    int pos = spinnerResidencias.getSelectedItemPosition();
                    if (valid && pos >= 0) {
                        addResident(residences.get(pos).getId(), name, surnames, fechaNacimiento, idDocument, family1, family2, year, month);
                    }
                });

                break;

            case "action_delete_resident":
                setupResidenciaSpinner(spinnerResidencias);
                spinnerResidencias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        setupResidenteSpinner(spinnerExtra, residences.get(position).getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
                executeBtn.setOnClickListener(v -> {
                    List<Long> residentIds = (List<Long>) spinnerExtra.getTag();
                    int pos = spinnerExtra.getSelectedItemPosition();
                    if (residentIds != null && pos >= 0 && pos < residentIds.size()) {
                        deleteResidente(residences.get(spinnerResidencias.getSelectedItemPosition()).getId(), residentIds.get(pos));
                    }
                });
                break;

            case "action_add_game":
                layout.findViewById(R.id.layout_game_inputs).setVisibility(View.VISIBLE);
                EditText gameName = layout.findViewById(R.id.et_add_game_nombre);
                executeBtn.setOnClickListener( v -> {
                    String name = gameName.getText().toString().trim();
                    if (name.isEmpty()) {
                        gameName.setError("Nombre obligatorio");
                    } else {
                        AppDataRepository.getInstance().addGame(requireContext(), name,
                                () -> {
                                    Toast.makeText(requireContext(), "Juego añadido", Toast.LENGTH_SHORT).show();
                                    refreshData();
                                },
                                () -> Toast.makeText(requireContext(), "Error al añadir juego", Toast.LENGTH_SHORT).show());
                    }
                });
                break;

            case "action_delete_game":
                spinnerResidencias.setVisibility(View.GONE);
                setupGameSpinner(spinnerExtra);
                executeBtn.setOnClickListener(v -> {
                    int pos = spinnerExtra.getSelectedItemPosition();
                    if (pos >= 0 && pos < games.size()) deleteJuego(games.get(pos).getId());
                });
                break;
        }
    }

    private void refreshData() {
        residences = AppDataRepository.getInstance().getResidences();
        residents = AppDataRepository.getInstance().getResidents();
        users = AppDataRepository.getInstance().getUsers();
        games = AppDataRepository.getInstance().getGames();

        for (Map.Entry<String, View> entry : actionViews.entrySet()) {
            setupActionLayout(entry.getKey(), entry.getValue());
        }
    }

    private void setupResidenciaSpinner(Spinner spinner) {
        spinner.setVisibility(View.VISIBLE);
        List<String> nombres = new ArrayList<>();
        for (Residence r : residences) nombres.add(r.getName());
        spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombres));
    }

    private void setupResidenteSpinner(Spinner spinner, long idResidencia) {
        spinner.setVisibility(View.VISIBLE);
        List<String> nombres = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (Resident r : residents) {
            if (r.getResidenceId() == idResidencia) {
                nombres.add(r.getName() + " " + r.getSurnames());
                ids.add(r.getId());
            }
        }
        spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombres));
        spinner.setTag(ids);
    }

    private void setupGameSpinner(Spinner spinner) {
        spinner.setVisibility(View.VISIBLE);
        List<String> nombres = new ArrayList<>();
        for (Game g : games) nombres.add(g.getName());
        spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombres));
    }

    private void setupUserSpinner(Spinner spinner, long idResidencia) {
        spinner.setVisibility(View.VISIBLE);
        List<User> filteredUsers = new ArrayList<>();
        for (User u : users) {
            if (u.getResidenceId() != null && u.getResidenceId() == idResidencia) {
                filteredUsers.add(u);
            }
        }
        if (filteredUsers.isEmpty()) {
            spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, new String[]{"No hay usuarios"}));
        } else {
            List<String> nombres = new ArrayList<>();
            for (User u : filteredUsers) {
                nombres.add(u.getName() + " " + u.getSurnames());
            }
            spinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, nombres));
            spinner.setTag(filteredUsers);
        }
    }

    private void addResidencia(String name, String email) {
        AppDataRepository.getInstance().addResidence(requireContext(), name, email,
                () -> {
                    Toast.makeText(requireContext(), "Residencia añadida", Toast.LENGTH_SHORT).show();
                    refreshData();
                },
                () -> Toast.makeText(requireContext(), "Error al añadir residencia", Toast.LENGTH_SHORT).show());
    }

    private void deleteResidencia(long id) {
        AppDataRepository.getInstance().deleteResidence(requireContext(), id,
                () -> {
                    Toast.makeText(requireContext(), "Residencia eliminada", Toast.LENGTH_SHORT).show();
                    refreshData();
                },
                () -> Toast.makeText(requireContext(), "Error al eliminar residencia", Toast.LENGTH_SHORT).show());
    }

    private void addResident(long idResi, String name, String surnames, LocalDate fechaNac, String idDocument, String family1, String family2, int documentYear, int documentMonth) {
        AppDataRepository.getInstance().addResidenteAdmin(requireContext(), idResi, name, surnames, fechaNac, idDocument, family1, family2, documentYear, documentMonth,
                () -> {
                    Toast.makeText(requireContext(), "Residente añadido", Toast.LENGTH_SHORT).show();
                    refreshData();
                },
                () -> Toast.makeText(requireContext(), "Error al añadir residente", Toast.LENGTH_SHORT).show());
    }

    private void deleteResidente(long idResi, long idResident) {
        AppDataRepository.getInstance().deleteResident(requireContext(), idResi, idResident,
                () -> {
                    Toast.makeText(requireContext(), "Residente eliminado", Toast.LENGTH_SHORT).show();
                    refreshData();
                },
                () -> Toast.makeText(requireContext(), "Error al eliminar residente", Toast.LENGTH_SHORT).show());
    }

    private void deleteJuego(long id) {
        AppDataRepository.getInstance().deleteGame(requireContext(), id,
                () -> {
                    Toast.makeText(requireContext(), "Juego eliminado", Toast.LENGTH_SHORT).show();
                    refreshData();
                },
                () -> Toast.makeText(requireContext(), "Error al eliminar juego", Toast.LENGTH_SHORT).show());
    }

    private void deleteUser(long idResi, long idUser) {
        AppDataRepository.getInstance().deleteUser(requireContext(), idResi, idUser,
                () -> {
                    Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show();
                    refreshData();
                },
                () -> Toast.makeText(requireContext(), "Error al eliminar usuario", Toast.LENGTH_SHORT).show());
    }

    private String formatActionTitle(String idName) {
        return idName.replace("action_", "").replace("_", " ").toUpperCase();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        residences = AppDataRepository.getInstance().getResidences();
        residents = AppDataRepository.getInstance().getResidents();
        users = AppDataRepository.getInstance().getUsers();
        games = AppDataRepository.getInstance().getGames();

        listener = (IOnAdminPanel) context;
    }
}
