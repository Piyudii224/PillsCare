package com.crackint.pillscare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RemaindersActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    private RecyclerView recyclerView;
    private MedicineAdapter medicineAdapter;
    private FirebaseAuth auth;
    private DatabaseReference dbMedicines;
    private List<Medicine> medicineList = new ArrayList<>();
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remainders);

        // ✅ Notification permission check for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        recyclerView = findViewById(R.id.recyclerViewMedicines);
        fab = findViewById(R.id.fabAddReminder);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(RemaindersActivity.this, AddMedicineActivity.class);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        auth = FirebaseAuth.getInstance();
        dbMedicines = FirebaseDatabase.getInstance().getReference("medicines");

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.navigation_remainders);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_profile) {
                startActivity(new Intent(RemaindersActivity.this, ProfileActivity.class));
                return true;
            } else if (id == R.id.navigation_home) {
                startActivity(new Intent(RemaindersActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.navigation_favorites) {
                startActivity(new Intent(RemaindersActivity.this, FavoritesActivity.class));
                return true;
            }
            return false;
        });

        loadMedicines();
    }

    private void loadMedicines() {
        String userId = auth.getCurrentUser().getUid();

        dbMedicines.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                medicineList.clear();
                for (DataSnapshot medSnapshot : snapshot.getChildren()) {
                    Medicine medicine = medSnapshot.getValue(Medicine.class);
                    if (medicine != null) {
                        medicineList.add(medicine);
                    }
                }

                medicineAdapter = new MedicineAdapter(medicineList, new MedicineAdapter.MedicineClickListener() {
                    @Override
                    public void onEdit(Medicine medicine) {
                        editMedicine(medicine);
                    }

                    @Override
                    public void onDelete(Medicine medicine) {
                        deleteMedicine(medicine);
                    }
                });

                recyclerView.setAdapter(medicineAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RemaindersActivity.this, "Failed to load medicines", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editMedicine(Medicine medicine) {
        Intent intent = new Intent(this, AddMedicineActivity.class);
        intent.putExtra("medicine", medicine); // Ensure Medicine implements Serializable or Parcelable
        startActivity(intent);
    }

    private void deleteMedicine(Medicine medicine) {
        String userId = auth.getCurrentUser().getUid();
        if (medicine.getId() == null) {
            Toast.makeText(this, "Medicine ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        dbMedicines.child(userId).child(medicine.getId())
                .removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show());
    }

    // ✅ Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
