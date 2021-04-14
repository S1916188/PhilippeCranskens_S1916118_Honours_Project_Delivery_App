package com.example.pizzadeliveryapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import java.text.ParseException;
import java.util.ArrayList;

public class SessionsFragment extends Fragment {
    //variables
    String username = "";
    ListView lv;
    TextView txtSessionWelcome;
    Button btnLogout;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_sessions, container, false); // creates view for fragment

        //finds controls from xml
        lv = (ListView) view.findViewById(R.id.lstSessions);
        txtSessionWelcome = (TextView) view.findViewById(R.id.txtSessionWelcome);
        txtSessionWelcome.setText("Displays any active sessions below.");
        btnLogout = (Button) view.findViewById(R.id.btnSessionLogout);

        //sets the list view to null to ensure no duplication of data
        lv.setAdapter(null);
        //getting username from intent
        try
        {
            username = getArguments().getString("Username");
        }
        catch (Exception ex)
        {
            Log.d("State", ex.toString());
        }

        DBHandler db = new DBHandler(getActivity()); // creates new dbhandler instance
        Session session = db.getSession(username); // gets current session from logged in username
        User user = db.getUser(username); // gets user from logged in username
        final ArrayList<Session> allSessions = db.getAllSessions(user); // gets all sessions for the current user
        ArrayList<String> sessionDates = new ArrayList<String>(); // creates new string array list
        for(Session eachSession : allSessions) // for each session in all sessions
        {
            sessionDates.add("Date: " + eachSession.getDate() + "        No. of Deliveries: " + eachSession.getDeliveries()); // add line to string array
        }

        // Create an ArrayAdapter from List
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (getActivity(), android.R.layout.simple_list_item_1, sessionDates)
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

        lv.setAdapter(arrayAdapter); // sets the array adapter to the array adapter created above.

        //Sets onclick listener for the listview
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(view.getContext(), SessionDetails.class);
                //gets the session at the position clicked
                Session passedSession = allSessions.get(position);
                //puts the session in the intent
                intent.putExtra("username", username);
                intent.putExtra("session", passedSession);
                //starts activity with intent.
                startActivity(intent);
            }
        });

        // BUTTON FOR LOGGING OUT
        btnLogout.setOnClickListener(new View.OnClickListener()
        {
            DBHandler db = new DBHandler(getActivity()); // Creates new instance of DBHandler
            Session runningSession = db.getSession(username); // get's current session
            @Override
            public void onClick(View v)
            {
                // Build an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select your answer.");
                builder.setMessage("Are you sure you want to log out?");
                // Set the alert dialog yes button click listener
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Intent myIntent = new Intent(getActivity(), MainActivity.class);
                        startActivityForResult(myIntent, 0);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        return;
                    }
                });
                AlertDialog dialog = builder.create();
                // Display the alert dialog on interface
                dialog.show();

            }});
        return view;
    }
    //Toast method
    private void showToast(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
