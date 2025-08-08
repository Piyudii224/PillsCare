package com.crackint.pillscare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private RecyclerView recyclerView;
    private MedicineSimpleAdapter adapter;
    private List<Medicine> upcomingMedicines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkNotificationPermission();

        recyclerView = findViewById(R.id.recyclerUpcomingMedicines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineSimpleAdapter(upcomingMedicines);
        recyclerView.setAdapter(adapter);

        loadMedicines();

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.navigation_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            } else if (id == R.id.navigation_remainders) {
                startActivity(new Intent(MainActivity.this, RemaindersActivity.class));
                return true;
            } else if (id == R.id.navigation_favorites) {
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
                return true;
            } else if (id == R.id.navigation_home) {
                return true;
            }
            return false;
        });
    }

    private void loadMedicines() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("medicines").child(uid);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upcomingMedicines.clear();

                String currentDay = new SimpleDateFormat("EEE", Locale.getDefault()).format(new Date()); // e.g., "Mon"
                long currentTimeMillis = System.currentTimeMillis();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Medicine med = snap.getValue(Medicine.class);
                    if (med != null && med.getDays() != null && med.getDays().contains(currentDay)) {
                        for (String timeStr : med.getTimes()) {
                            long timeMillis = convertTimeToMillis(timeStr);
                            if (timeMillis > currentTimeMillis) {
                                // Clone medicine and keep only upcoming time
                                Medicine upcomingMed = new Medicine();
                                upcomingMed.setName(med.getName());
                                upcomingMed.setTimes(Collections.singletonList(timeStr));
                                upcomingMedicines.add(upcomingMed);
                            }
                        }
                    }
                }

                // Sort by upcoming time
                upcomingMedicines.sort(Comparator.comparingLong(med -> convertTimeToMillis(med.getTimes().get(0))));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private long convertTimeToMillis(String timeStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf.parse(timeStr);
            if (date != null) {
                Calendar now = Calendar.getInstance();
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
                cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
                cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
                return cal.getTimeInMillis();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher = registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {}
                );
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
