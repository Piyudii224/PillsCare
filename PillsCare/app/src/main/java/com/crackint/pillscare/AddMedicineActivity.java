package com.crackint.pillscare;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddMedicineActivity extends AppCompatActivity {

    private EditText etName;
    private LinearLayout timesContainer;
    private CheckBox checkMon, checkTue, checkWed, checkThu, checkFri, checkSat, checkSun;
    private Spinner spinnerFrequency;
    private Button btnAdd;

    private FirebaseAuth auth;
    private DatabaseReference dbMedicines;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        etName          = findViewById(R.id.etMedicineName);
        timesContainer  = findViewById(R.id.timesContainer);
        checkMon        = findViewById(R.id.checkMon);
        checkTue        = findViewById(R.id.checkTue);
        checkWed        = findViewById(R.id.checkWed);
        checkThu        = findViewById(R.id.checkThu);
        checkFri        = findViewById(R.id.checkFri);
        checkSat        = findViewById(R.id.checkSat);
        checkSun        = findViewById(R.id.checkSun);
        spinnerFrequency= findViewById(R.id.spinnerFrequency);
        btnAdd          = findViewById(R.id.btnAdd);

        auth        = FirebaseAuth.getInstance();
        dbMedicines = FirebaseDatabase.getInstance().getReference("medicines");

        // frequency dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Once a day", "Twice a day", "Thrice a day"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);

        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { setupTimePickers(pos + 1); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        btnAdd.setOnClickListener(v -> saveMedicine());
    }

    /** Create n disabled EditTexts that open a time-picker */
    private void setupTimePickers(int count) {
        timesContainer.removeAllViews();
        for (int i = 0; i < count; i++) {
            EditText etTime = new EditText(this);
            etTime.setFocusable(false);
            etTime.setClickable(true);
            etTime.setHint("Select Time " + (i + 1));
            etTime.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            etTime.setOnClickListener(v -> showTimePicker(etTime));
            timesContainer.addView(etTime);
        }
    }

    /** Opens a 12-hour TimePicker and writes “hh:mm a” into the EditText */
    private void showTimePicker(EditText target) {
        Calendar now = Calendar.getInstance();
        int h = now.get(Calendar.HOUR_OF_DAY), m = now.get(Calendar.MINUTE);

        TimePickerDialog dlg = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    Calendar sel = Calendar.getInstance();
                    sel.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    sel.set(Calendar.MINUTE, minute);
                    String formatted = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                            .format(sel.getTime());
                    target.setText(formatted);
                },
                h, m, false);               // false → 12-hour dialog with AM/PM
        dlg.show();
    }

    /** Validates input, saves to Firebase, then schedules notifications */
    private void saveMedicine() {
        String name = etName.getText().toString().trim();

        // collect times
        List<String> times = new ArrayList<>();
        for (int i = 0; i < timesContainer.getChildCount(); i++) {
            EditText et = (EditText) timesContainer.getChildAt(i);
            String t = et.getText().toString().trim();
            if (TextUtils.isEmpty(t)) {
                Toast.makeText(this, "Please select all times", Toast.LENGTH_SHORT).show();
                return;
            }
            times.add(t);
        }

        // collect selected days
        List<String> days = new ArrayList<>();
        if (checkMon.isChecked()) days.add("Mon");
        if (checkTue.isChecked()) days.add("Tue");
        if (checkWed.isChecked()) days.add("Wed");
        if (checkThu.isChecked()) days.add("Thu");
        if (checkFri.isChecked()) days.add("Fri");
        if (checkSat.isChecked()) days.add("Sat");
        if (checkSun.isChecked()) days.add("Sun");

        if (TextUtils.isEmpty(name) || days.isEmpty()) {
            Toast.makeText(this, "Please enter name and select day(s)", Toast.LENGTH_SHORT).show();
            return;
        }

        String frequency = (spinnerFrequency.getSelectedItem() != null)
                ? spinnerFrequency.getSelectedItem().toString() : "";

        String id   = UUID.randomUUID().toString();
        String user = auth.getCurrentUser().getUid();

        Medicine med = new Medicine(id, name, times, days, frequency);

        dbMedicines.child(user).child(id).setValue(med)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Medicine added!", Toast.LENGTH_SHORT).show();

                    // ➜ Schedule WorkManager notifications
                    ReminderScheduler.scheduleReminder(this, med);

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
