package com.example.pizzadeliveryapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SessionDetails extends AppCompatActivity
{
    // Global variables and controls
    String username = "";
    ListView lv;
    Session passedSession;
    ImageButton backButton;
    TextView distance, duration, txtWelcome;

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // hide the title(app name)
        getSupportActionBar().hide(); // Hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.session_info);

        // Finding controls
        ListView lv = (ListView) findViewById(R.id.lstDeliveries);
        backButton = (ImageButton) findViewById(R.id.btnBackToSessions);
        distance = (TextView) findViewById(R.id.txtDistance);
        duration = (TextView) findViewById(R.id.txtDuration);
        txtWelcome = (TextView) findViewById(R.id.txtDeliveryWelcome);

        try
        {
            username = getIntent().getStringExtra("username"); // get username passed in intent
            passedSession = (Session) getIntent().getSerializableExtra("session"); // get session passed in intent
        }
        catch (Exception ex)
        {
            Log.d("State", ex.toString()); // Logs exception details
        }

        lv.setAdapter(null); // sets adapter to null
        DBHandler db = new DBHandler(this); // new isntance of dbhandler
        ArrayList<Delivery> allDeliveries = db.getAllDeliveries(passedSession); // Gets all deliveries for selected session and assigns them to new array list for deliveries
        ArrayList<String> deliveryDetails = new ArrayList<String>(); // new string array list to store delivery details
        int totalDuration = 0; // initialise variable for storing duration of all deliveries
        double totalDistance = 0; // initialise variable to store total delivery distance
        for(Delivery eachDelivery : allDeliveries) // for each delivery in all deliveries
        {
            // Get each deliveries address, start time, end time, duration and distance
            // Format them into a String message for display, then add the string to the string array declared above deliveryDetails.
            deliveryDetails.add("Address: " + eachDelivery.getAddress() + " | Start Time: " + eachDelivery.getStartTime() + " | End Time: " + eachDelivery.getDeliveredTime()
            + " Duration: " + eachDelivery.getDuration() + " | Distance in KM: " + eachDelivery.getDistance());
            totalDuration += Integer.valueOf(eachDelivery.getDuration()); // Adds the duration cumulatively to the totalDuration variable
            totalDistance += Double.valueOf(eachDelivery.getDistance()); // Adds the distance cumulatively to the totalDuration variable
        }

        // Set's TextViews with appropriate info
        txtWelcome.setText("This page displays all deliveries performed for the selected session");
        duration.setText("Total Duration for deliveries: " + totalDuration + " minutes.");
        distance.setText("Total Distance for deliveries: " + totalDistance + " Km");

        // Create an ArrayAdapter from List
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, deliveryDetails)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);
                String text = tv.getText().toString();

                //returns the view, allowing list to be displayed
                return view;
            }
        };
        //Log.d("Checking username", username);
        lv.setAdapter(arrayAdapter);

        backButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish(); // logs user out
            }
        });

    }
}
