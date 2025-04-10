package com.example.ptyxiakh;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.ptyxiakh.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;

public class DashboardActivity extends AppCompatActivity {

    // Firebase Auth
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;

    String mUID;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        // Set up the support action bar
        setSupportActionBar(findViewById(R.id.toolbar));

        // ActionBar and its title
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Home");
        }
        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Bottom Navigation
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);


        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();

        checkUserStatus();


    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private final OnNavigationItemSelectedListener selectedListener = new OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            int itemId = menuItem.getItemId();

            if (itemId == R.id.nav_home) {
                actionBar.setTitle("Home"); // Change ActionBar title
                HomeFragment fragment1 = new HomeFragment();
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.content, fragment1, "");
                ft1.commit();
                return true;
            } else if (itemId == R.id.nav_profile) {
                actionBar.setTitle("Profile"); // Change ActionBar title
                ProfileFragment fragment2 = new ProfileFragment();
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.replace(R.id.content, fragment2, "");
                ft2.commit();
                return true;

            } else if (itemId == R.id.nav_users) {
                actionBar.setTitle("Users"); // Change ActionBar title
                UsersFragment fragment3 = new UsersFragment();
                FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                ft3.replace(R.id.content, fragment3, "");
                ft3.commit();
                return true;
            } else if (itemId == R.id.nav_chat) {
                actionBar.setTitle("Chats"); // Change ActionBar title
                ChatListFragment fragment4 = new ChatListFragment();
                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                ft4.replace(R.id.content, fragment4, "");
                ft4.commit();
                return true;
            }

            return false;
        }
    };

    private void checkUserStatus() {
        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in, stay here
            // Set email of logged-in user
            mUID = user.getUid();

            SharedPreferences sp = getSharedPreferences("SP_User", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            // Update token
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String tokenRefresh = task.getResult();
                    if (tokenRefresh != null) {
                        updateToken(tokenRefresh);
                    }
                }
            });


        } else {
            // User not signed in, go to main activity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        // Check on start of app
        checkUserStatus();
        super.onStart();
    }
}
