package com.andresgmoran.apptrabajadores.ui.fragments.resident;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.andresgmoran.apptrabajadores.R;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AddResidentFragment extends Fragment {

    public interface OnAddResidentListener {
        void onAddResidentFormSubmitted(
                String nombre,
                String apellido,
                LocalDate fechaNacimiento,
                String documentoIdentidad,
                String familiar1,
                String familiar2,
                int year,
                int month
        );
    }

    private OnAddResidentListener listener;

    private EditText nombreEditText;
    private EditText apellidoEditText;
    private EditText fechaNacimientoEditText;
    private EditText documentoIdentidadEditText;
    private EditText familiar1EditText;
    private EditText familiar2EditText;
    private EditText yearEditText;
    private EditText monthEditText;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnAddResidentListener) {
            listener = (OnAddResidentListener) context;
        } else {
            throw new RuntimeException(context + " debe implementar OnAddResidentListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_resident, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nombreEditText = view.findViewById(R.id.et_add_resident_nombre);
        apellidoEditText = view.findViewById(R.id.et_add_resident_apellido);
        fechaNacimientoEditText = view.findViewById(R.id.et_add_resident_fecha_nacimiento);
        documentoIdentidadEditText = view.findViewById(R.id.et_add_resident_documento_identidad);
        familiar1EditText = view.findViewById(R.id.et_add_resident_familiar_1);
        familiar2EditText = view.findViewById(R.id.et_add_resident_familiar_2);
        yearEditText = view.findViewById(R.id.et_add_resident_year);
        monthEditText = view.findViewById(R.id.et_add_resident_month);

        Button guardarButton = view.findViewById(R.id.btn_add_resident_guardar);
        guardarButton.setOnClickListener(v -> {
            if (validarCampos()) {
                try {
                    String nombre = nombreEditText.getText().toString().trim();
                    String apellido = apellidoEditText.getText().toString().trim();
                    LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoEditText.getText().toString().trim());
                    String documento = documentoIdentidadEditText.getText().toString().trim();
                    String familiar1 = familiar1EditText.getText().toString().trim();
                    String familiar2 = familiar2EditText.getText().toString().trim();
                    int year = Integer.parseInt(yearEditText.getText().toString().trim());
                    int month = Integer.parseInt(monthEditText.getText().toString().trim());

                    listener.onAddResidentFormSubmitted(
                            nombre, apellido, fechaNacimiento, documento, familiar1, familiar2, year, month
                    );
                } catch (DateTimeParseException e) {
                    fechaNacimientoEditText.setError("Formato inválido (usar YYYY-MM-DD)");
                } catch (NumberFormatException e) {
                    if (!isInteger(yearEditText.getText().toString().trim())) {
                        yearEditText.setError("Año inválido");
                    }
                    if (!isInteger(monthEditText.getText().toString().trim())) {
                        monthEditText.setError("Mes inválido");
                    }
                }
            }
        });
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (TextUtils.isEmpty(nombreEditText.getText())) {
            nombreEditText.setError("Campo obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(apellidoEditText.getText())) {
            apellidoEditText.setError("Campo obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(fechaNacimientoEditText.getText())) {
            fechaNacimientoEditText.setError("Campo obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(documentoIdentidadEditText.getText()) || documentoIdentidadEditText.getText().length() != 8) {
            documentoIdentidadEditText.setError("Campo obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(familiar1EditText.getText())) {
            familiar1EditText.setError("Campo obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(familiar2EditText.getText())) {
            familiar2EditText.setError("Campo obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(yearEditText.getText()) || Integer.parseInt( yearEditText.getText().toString().trim()) < LocalDate.now().getYear() || !isInteger(yearEditText.getText().toString().trim())) {
            yearEditText.setError("Campo obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(monthEditText.getText()) || Integer.parseInt(monthEditText.getText().toString().trim()) < 1 || Integer.parseInt(monthEditText.getText().toString().trim()) > 12 || !isInteger(monthEditText.getText().toString().trim())) {
            monthEditText.setError("Campo obligatorio");
            valido = false;
        }

        return valido;
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
