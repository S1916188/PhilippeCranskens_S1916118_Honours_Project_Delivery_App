package com.example.pizzadeliveryapp;

import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title(app name)
        getSupportActionBar().hide(); // Hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_main);

        // Finding controls
        Button registerButton = (Button) findViewById(R.id.btnRegister);
        Button loginButton = (Button) findViewById(R.id.btnLogin);

        //Register Button
        registerButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent myIntent = new Intent(view.getContext(), RegisterPage.class); // Creates intent for register page
                startActivityForResult(myIntent,0); // Launches intent activity
            }
        });

        // Login Button
        loginButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent myIntent = new Intent(view.getContext(), LoginPage.class); // creates intent for login page
                startActivityForResult(myIntent,0); // launches intent activity
            }
        });
    }
}
