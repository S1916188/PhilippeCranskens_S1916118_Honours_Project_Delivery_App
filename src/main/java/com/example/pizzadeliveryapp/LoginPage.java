package com.example.pizzadeliveryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class LoginPage extends AppCompatActivity
{
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title(app name)
        getSupportActionBar().hide(); // Hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.login_page);

        // Find controls from xml
        Button loginRegister = (Button) findViewById(R.id.btnLoginRegister);
        Button loginButton = (Button) findViewById(R.id.btnLogin);
        ImageButton backButton = (ImageButton) findViewById(R.id.btnBack);
        final EditText txtEmail = (EditText) findViewById(R.id.txtLoginEmail);
        final EditText txtPassword = (EditText) findViewById(R.id.txtLoginPassword);

        loginButton.setOnClickListener(new View.OnClickListener() // onclick listener for login button
        {
            public void onClick(View view)
            {
                String email = txtEmail.getText().toString(); // assign email variable text value from email edit text
                email = email.replaceAll("\\s+", ""); // Replace white space in email string
                String password = txtPassword.getText().toString(); // assign password variable value from edit text
                password = password.replaceAll("\\s+", ""); // replace white space in email variable

                DBHandler db = new DBHandler(LoginPage.this); // new instance of DBHandler
                try
                {
                    boolean userCheck = db.userLookup(email, password); // calling db method to check for user
                    if (userCheck) // if user exists
                    {
                        //Toast message informing the user they have logged in successfully
                        Toast toast= Toast.makeText(getApplicationContext(),
                                "Login Success!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 80);
                        toast.show();

                        String username = db.getUsername(email); // get the username from the logged in users email address

                        Intent myIntent = new Intent(view.getContext(), HomePage.class); // create an intent to navigate to home page activity.
                        myIntent.putExtra("Username", username); // puts username in intent
                        startActivityForResult(myIntent, 0); // start new activity
                    }
                    else // if user does not exist
                    {
                        // Toast informing the user that their login details are incorrect
                        Toast toast= Toast.makeText(getApplicationContext(),
                                "Login Details Are Incorrect", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 80);
                        toast.show();
                    }

                } // end of try
                catch (Exception ex)
                {
                    Toast.makeText(LoginPage.this, ex.toString(), Toast.LENGTH_LONG).show(); // toasts the exception details
                }
            }
        });

        // Listener event for register button to navigate to registration page.
        loginRegister.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent myIntent = new Intent(view.getContext(), RegisterPage.class);
                startActivityForResult(myIntent,0);
            }
        });

        // back button to return to main menu
        backButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent myIntent = new Intent(view.getContext(), MainActivity.class);
                startActivityForResult(myIntent,0);
            }
        });
    }
}
