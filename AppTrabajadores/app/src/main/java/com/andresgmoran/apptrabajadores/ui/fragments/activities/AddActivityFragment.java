package com.andresgmoran.apptrabajadores.ui.fragments.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.andresgmoran.apptrabajadores.R;

import java.time.LocalDateTime;
import java.util.Calendar;

public class AddActivityFragment extends Fragment {

    public interface IOnAddActivity {
        void onAddActivity(String activityName, String activityDescription, LocalDateTime dateTime);
    }

    private IOnAddActivity listener;

    private Button buttonFecha;
    private EditText nameEditText;
    private EditText descriptionEditText;
    private LocalDateTime selectedDateTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameEditText = view.findViewById(R.id.add_activity_name_input);
        descriptionEditText = view.findViewById(R.id.add_activity_description_input);
        buttonFecha = view.findViewById(R.id.buttonFecha);
        Button buttonAgregar = view.findViewById(R.id.save_new_activity_button);

        buttonFecha.setOnClickListener(v -> showDatePickerDialog());

        buttonAgregar.setOnClickListener(v -> {
            if (validarCampos()) {
                String activityName = nameEditText.getText().toString().trim();
                String activityDescription = descriptionEditText.getText().toString().trim();

                listener.onAddActivity(activityName, activityDescription, selectedDateTime);
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(
                requireContext(),
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) -> {
                    showTimePickerDialog(selectedYear, selectedMonth + 1, selectedDayOfMonth);
                },
                year, month, day
        ).show();
    }

    private void showTimePickerDialog(int year, int month, int dayOfMonth) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(
                requireContext(),
                (TimePicker view, int selectedHour, int selectedMinute) -> {
                    selectedDateTime = LocalDateTime.of(year, month, dayOfMonth, selectedHour, selectedMinute);
                    buttonFecha.setText(selectedDateTime.toString().replace('T', ' '));
                },
                hour, minute, true
        ).show();
    }

    private boolean validarCampos() {
        boolean valido = true;

        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameEditText.setError("Campo obligatorio");
            valido = false;
        }
        if (TextUtils.isEmpty(descriptionEditText.getText())) {
            descriptionEditText.setError("Campo obligatorio");
            valido = false;
        }
        if (selectedDateTime == null) {
            buttonFecha.setError("Selecciona fecha y hora");
            valido = false;
        } else {
            buttonFecha.setError(null); // limpia error si está bien
        }

        return valido;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IOnAddActivity) {
            listener = (IOnAddActivity) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement IOnAddActivity");
        }
    }
}
