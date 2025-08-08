package com.crackint.pillscare;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class FavoritesActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    MaterialCalendarView calendarView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        calendarView = findViewById(R.id.calendarView);
        bottomNav = findViewById(R.id.bottomNav);

        loadTakenMedicineDates();

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            showTakenMedicinesForDate(date);
        });

        bottomNav.setSelectedItemId(R.id.navigation_favorites);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_profile) {
                startActivity(new Intent(FavoritesActivity.this, ProfileActivity.class));
                return true;
            } else if (id == R.id.navigation_remainders) {
                startActivity(new Intent(FavoritesActivity.this, RemaindersActivity.class));
                return true;
            } else if (id == R.id.navigation_home) {
                startActivity(new Intent(FavoritesActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.navigation_favorites) {
                return true;
            }
            return false;
        });
    }

    private void loadTakenMedicineDates() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference historyRef = FirebaseDatabase.getInstance()
                .getReference("takenHistory").child(userId);

        historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                HashSet<CalendarDay> takenDates = new HashSet<>();

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String[] parts = dateSnapshot.getKey().split("-");
                    if (parts.length == 3) {
                        int year = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]) - 1;
                        int day = Integer.parseInt(parts[2]);
                        takenDates.add(CalendarDay.from(year, month, day));
                    }
                }

                int dotColor = Color.parseColor("#FF4081");
                calendarView.addDecorator(new EventDecorator(dotColor, takenDates));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavoritesActivity.this, "Failed to load taken dates", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTakenMedicinesForDate(CalendarDay date) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                date.getYear(), date.getMonth() + 1, date.getDay());

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("takenHistory")
                .child(userId)
                .child(dateStr);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(FavoritesActivity.this, "No medicines taken on this day", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Collect medicine names for suggestions
                ArrayList<String> medicineNames = new ArrayList<>();
                for (DataSnapshot medSnapshot : snapshot.getChildren()) {
                    TakenMedicine med = medSnapshot.getValue(TakenMedicine.class);
                    if (med != null && med.getMedicineName() != null) {
                        medicineNames.add(med.getMedicineName());
                    }
                }

                AutoCompleteTextView input = new AutoCompleteTextView(FavoritesActivity.this);
                input.setHint("Filter by medicine name (optional)");
                input.setAdapter(new ArrayAdapter<>(FavoritesActivity.this, android.R.layout.simple_dropdown_item_1line, medicineNames));
                input.setThreshold(1);

                new AlertDialog.Builder(FavoritesActivity.this)
                        .setTitle("Filter Medicines")
                        .setView(input)
                        .setPositiveButton("Show", (dialog, which) -> {
                            String filter = input.getText().toString().trim().toLowerCase(Locale.getDefault());
                            StringBuilder builder = new StringBuilder();
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                            for (DataSnapshot medSnapshot : snapshot.getChildren()) {
                                TakenMedicine med = medSnapshot.getValue(TakenMedicine.class);
                                if (med != null && med.getMedicineName() != null) {
                                    String name = med.getMedicineName();
                                    if (filter.isEmpty() || name.toLowerCase().contains(filter)) {
                                        String timeTaken = timeFormat.format(new Date(med.getTakenAt()));
                                        builder.append("- ").append(name).append(" at ").append(timeTaken).append("\n");
                                    }
                                }
                            }

                            if (builder.length() == 0) {
                                builder.append("No medicines match your filter.");
                            }

                            new AlertDialog.Builder(FavoritesActivity.this)
                                    .setTitle("Taken on " + dateStr)
                                    .setMessage(builder.toString())
                                    .setPositiveButton("OK", null)
                                    .show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavoritesActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}