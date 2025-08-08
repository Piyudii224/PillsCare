package com.crackint.pillscare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvName;
    private Button btLogout;
    private BottomNavigationView bottomNav;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivImage = findViewById(R.id.iv_image);
        tvName = findViewById(R.id.tv_name);
        btLogout = findViewById(R.id.bt_logout);
        bottomNav = findViewById(R.id.bottomNav);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            if (firebaseUser.getPhotoUrl() != null) {
                String photoUrl = firebaseUser.getPhotoUrl().toString();
                photoUrl = photoUrl.replace("s96-c", "s400-c");

                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .into(ivImage);
            }

            String name = firebaseUser.getDisplayName();
            tvName.setText(name != null && !name.isEmpty() ? name : "Hello User 👋");
        }

        btLogout.setOnClickListener(view -> {
            firebaseAuth.signOut();
            Toast.makeText(getApplicationContext(), "Logout successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        bottomNav.setSelectedItemId(R.id.navigation_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.navigation_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_remainders) {
                startActivity(new Intent(ProfileActivity.this, RemaindersActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_favorites) {
                startActivity(new Intent(ProfileActivity.this, FavoritesActivity.class));
                finish();
                return true;
            } else if (id == R.id.navigation_profile) {
                return true; // Already here
            }

            return false;
        });
    }
}
