package com.example.pizzadeliveryapp;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterPage extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title(app name)
        getSupportActionBar().hide(); // Hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.register_page);

        // finding Controls
        final Button registerButton = (Button) findViewById(R.id.btnRegister);
        final ImageButton backButton = (ImageButton) findViewById(R.id.btnBack);
        final EditText txtUsername = (EditText) findViewById(R.id.txtRegisterUsername);
        final EditText txtEmail = (EditText) findViewById(R.id.txtRegisterEmail);
        final EditText txtPassword = (EditText) findViewById(R.id.txtRegisterPassword);

        // REGISTER BUTTON CLICK
        registerButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view) {
                User newUser = new User(); // new user
                DBHandler db = new DBHandler(RegisterPage.this); // new DBHandler

                try {
                    String username = txtUsername.getText().toString(); // gets username from username edit text
                    username = username.replaceAll("\\s+", ""); // eliminates white space and special characters
                    newUser.setUsername(username); // sets username to value without whitespace or special characters for the new user

                    String email = txtEmail.getText().toString(); // gets email from edit text
                    email = email.replaceAll("\\s+", ""); // eliminates white space and special characters

                    boolean emailChecking = db.getEmail(email); // checks db for email address
                    // Checks that the email entered is a valid email pattern, and that it is not in the db already
                    if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() && emailChecking == false)
                    {
                        newUser.setEmailAddress(email); // sets email to value without whitespace or special characters to the new user
                    }
                    else // email is not a valid pattern or in the db already
                        {
                            //informs the user
                        Toast.makeText(RegisterPage.this, "Email Address Entry is Invalid", Toast.LENGTH_LONG).show();
                        return;
                        }

                    String password = txtPassword.getText().toString(); // Gets password from password edittext
                    password = password.replaceAll("\\s+", ""); // removes white space and special characters

                    newUser.setPassword(password); // sets password for the new user

                    boolean success = db.addUser(newUser); // attempts to add user to the db
                    if (success) // if user entry is successful (true)
                    {
                        Toast.makeText(RegisterPage.this, "Success", Toast.LENGTH_SHORT).show(); // informs the user
                        Intent myIntent = new Intent(view.getContext(), LoginPage.class); // Creates new intent with the target activity being the login page
                        startActivityForResult(myIntent, 0); // starts intent activity
                    }
                    else // If user registration does not work (false)
                        {
                            // Inform the user
                        Toast.makeText(RegisterPage.this, "Error Registering to the database", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } // end of register try
                catch(Exception ex)
                {
                    Log.d("EXCEPTION REGISTER", ex.toString()); // catches exception in the Log
                }
            }
        });

        // BACK BUTTON
        backButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                //Return the user to the previosu screen
                Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                startActivityForResult(myIntent,0);
            }
        });

    }
}
