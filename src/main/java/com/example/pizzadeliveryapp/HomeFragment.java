package com.example.pizzadeliveryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;


public class HomeFragment extends Fragment
{
    //Declaring variables
    TextView welcomeView, sessionInfo;
    String username = "";
    Button startButton, endButton, logoutButton;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_home, container, false); // Creates view for fragment
        // Finding Controls
        startButton = (Button) view.findViewById(R.id.btnStartSession);
        endButton = (Button) view.findViewById(R.id.btnEndSession);
        logoutButton = (Button) view.findViewById(R.id.btnHomeLogout);
        welcomeView = view.findViewById(R.id.txtWelcome);
        sessionInfo = view.findViewById(R.id.txtSessionInfo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkPermission(); // calls checkPermissions method
        }

        try
        {
            username = getArguments().getString("Username"); // gets the username from the intent passed over.
        }
        catch (Exception ex)
        {
            Log.d("State", ex.toString());
        }

        // START SESSION BUTTON
        startButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v)
            {
                    //creates yes/no option dialogue
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Select your answer.");
                    builder.setMessage("Would you like to begin a new session?");
                    // Set the alert dialog yes button click listener
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() // if yes selected
                    {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            try
                            {
                                Session session; // create new session object
                                DBHandler db = new DBHandler(getActivity()); // create new DBHandler instance
                                int active = 1; // Set's active variable to 1 which is equal to true within the sql database
                                String date = ""; // initialise date variable
                                Calendar calender = Calendar.getInstance(); // new instance of Calender
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // Create new simple date format object, setting the date to yyyy-MM-dd
                                date = dateFormat.format(calender.getTime()); // formats date variable using the simple date format object

                                //Add the user details to the newUser object.
                                session = new Session(-1, active, date, LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
                                        LocalTime.of(23, 59), 0, db.getUser(username).getUserId());
                                //Log.d("SessionCheck", Calendar.getInstance().toString());

                                db.addSession(session); // adds session object to database

                                //refreshes fragment to view correct button determined by an active session or no session.
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.detach(HomeFragment.this).attach(HomeFragment.this).commit();
                            }
                            catch (Exception ex)
                            {
                                Log.d("StartSession", ex.toString());
                            }
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() // if no selected
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            return; // return
                        }
                    });
                    AlertDialog dialog = builder.create();
                    // Display the alert dialog on interface
                    dialog.show();
            }
        });

        // END SESSION BUTTON
        endButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                DBHandler db = new DBHandler(getActivity()); // new instance of DBHandler
                Session runningSession = db.getSession(username); // checks for running session
                Delivery unfinishedDelivery = db.getLastDelivery(runningSession); // checks for unfinished delivery
                if (unfinishedDelivery.getDuration().equalsIgnoreCase("Not Completed"))
                {
                    showToast("You have a delivery that is not marked completed, \n please complete this before ending a session"); // Informs user that they need to complete delivery
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // build alert dialog
                    builder.setTitle("Select your answer.");
                    builder.setMessage("Would you like to end your current session?");
                    // Set the alert dialog yes button click listener
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() { // if yes selected
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DBHandler db = new DBHandler(getActivity()); // create new instance of dbhandler
                            Session session = db.getSession(username); // get current session if there is one
                            try
                            {
                                db.endSession(session); // Ends current session
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.detach(HomeFragment.this).attach(HomeFragment.this).commit(); // refreshes fragment to view updated XML based on session status
                            }
                            catch (Exception ex)
                            {
                                Log.d("EndSession", ex.toString()); // logs exception details
                            }
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() { // if no selected
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            return; // return
                        }
                    });
                    AlertDialog dialog = builder.create();
                    // Display the alert dialog on interface
                    dialog.show();
                }
            }
        });

        // BUTTON FOR LOGGING OUT
        logoutButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v)
            {
                // Build an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select your answer.");
                builder.setMessage("Are you sure you want to log out?");
                // Set the alert dialog yes button click listener
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() // if yes selected
                {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent myIntent = new Intent(getActivity(), MainActivity.class); // set's the Main Activity as intent destination to log the user out
                        startActivityForResult(myIntent, 0); // starts intent
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() // if no selected
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        return; // return
                    }
                });
                AlertDialog dialog = builder.create();
                // Display the alert dialog on interface
                dialog.show();
            }});

        return view; // Returns fragment view
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    // When fragment view is created and returned
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        DBHandler db = new DBHandler(getActivity()); // new isntance of DDBHandler
        User loggedInUser = db.getUser(username); // gets logged in user
        try
        {
            Session runningSession = db.getSession(username); // gets current session
            if(runningSession.getSessionID() == 0) // if there is no session
            {
                endButton.setVisibility(View.GONE); // Hide the button to end session
                // Inform user there is no session and they need to start one
                sessionInfo.setText("You have no sessions running. " +
                        "\n Begin a session to track your deliveries using" +
                        "\n the start session button");
            }
            else
                {
                startButton.setVisibility(View.GONE); // If session exists, hide the start session button
                sessionInfo.setText("You have an active session that started at: " + db.getSession(username).getStartTime()); // inform user when they started current session
                }
        }
        catch(Exception ex)
        {
            Log.d("StartSession", ex.toString()); // logs exception details
        }
        welcomeView.setText("Welcome " + loggedInUser.getUsername()); // Sets welcome textview to a welcome message
    }

    //METHOD TO CHECK PHONE PERMISSION
    public void checkPermission()
    {
        //Check if fine and coarse permissions are granted already
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //If permissions not granted, request them from user
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    123);
        }
    }
//TOAST METHOD
    private void showToast(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
}
