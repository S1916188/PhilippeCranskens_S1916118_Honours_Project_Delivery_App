package com.example.pizzadeliveryapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayDeque;
import java.util.Deque;


public class HomePage extends AppCompatActivity
{
    private String username; // public String variable to hold username

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title(app name)
        getSupportActionBar().hide(); // Hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.home_page);

        //Finding controls
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        username = getIntent().getStringExtra("Username"); // gets username from login activity
        Bundle bundle = new Bundle(); // Creates new instance of bundle that will allow passing items to fragments
        bundle.putString("Username", username); // puts username in the bundle
        HomeFragment homeFrag = new HomeFragment(); // New instance of the home fragment screen
        homeFrag.setArguments(bundle); // put the username bundle into the instance of homeFragment

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, homeFrag).commit(); // Loads the Home Fragment

    }

    // Bottom navigation control switch case
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item)
                {
                    Fragment selectedFragment = null; // initialises a null fragment
                    Bundle bundle = new Bundle(); // creates a new bundle instance

                    switch (item.getItemId()) // switch statement to determine which navigation button is clicked
                    {
                        case R.id.nav_home: // If the home nav button is selected
                             bundle.putString("Username", username); // bundle the username
                             selectedFragment = new HomeFragment(); // sets the fragment choice as the home fragment
                             selectedFragment.setArguments(bundle); // apply bundle to the selected fragment
                        break;

                        case R.id.nav_camera: // If the camera nav button is selected
                            bundle.putString("Username", username); // bundle the username
                            selectedFragment = new CameraFragment(); // sets the fragment choice as the camera fragment
                            selectedFragment.setArguments(bundle); // apply bundle to the selected fragment
                            break;

                        case R.id.nav_sessions:  // If the camera nav button is selected
                            bundle.putString("Username", username);  // bundle the username
                            selectedFragment = new SessionsFragment(); // sets the fragment choice as the sessions fragment
                            selectedFragment.setArguments(bundle); // apply bundle to the selected fragment
                            break;
                    }

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment).commit(); // replaces the selected fragment with the chosen fragment value and then loads the new fragment

                        return true;
                    }
            };




    }
