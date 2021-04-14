package com.example.pizzadeliveryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CameraFragment extends Fragment implements LocationListener
{
    //Declaring variables
    ImageView displayImage;
    TextView displayText;
    String username = "", publicAddress = "", deliveryPostcode = "";
    double deliveryLat, deliveryLon;
    double phoneLat, phoneLon;
    private static final int REQUEST_IMAGE_CAPTURE = 111;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button btnCaptureImage, btnNavigation, btnMarkDeliveryComplete;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        //Creates fragment view
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        //Finding controls from XML
        btnCaptureImage = (Button) view.findViewById(R.id.btnCapture);
        btnNavigation = (Button) view.findViewById(R.id.btnNavigate);
        btnMarkDeliveryComplete = (Button) view.findViewById(R.id.btnComplete);

        //Allows current location of device to be found
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        //method to get location of phone
        getLocation();

        //Try catch for taking in username
        try
        {
            username = getArguments().getString("Username"); // gets username from intent and sets to global variable username
        }
        catch (Exception ex)
        {
            Log.d("State", ex.toString()); // captures error code in log
        }

        // Checks for camera permissions
        if (getActivity().checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            //grant permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
        }

        btnMarkDeliveryComplete.setVisibility(View.GONE); // Hides button for marking delivery complete
        DBHandler db = new DBHandler(getActivity()); // new instance of DBHandler
        Session sessionCheck = db.getSession(username); // Checks running session for logged in user
        Delivery deliveryInProgress; // creates new delivery instance
        if (sessionCheck != null) // if there is a session
        {
            deliveryInProgress = db.getLastDelivery(sessionCheck); // check if there is a delivery in progress already
            if(!deliveryInProgress.getAddress().isEmpty()) // if  delivery is in progress
            {
                btnMarkDeliveryComplete.setVisibility(View.VISIBLE); // set button visible
            }
        }

        //BUTTON FOR CAPTURING POSTCODE
        btnCaptureImage.setOnClickListener(new View.OnClickListener()
        {
            DBHandler db = new DBHandler(getActivity()); // new isntance of db
            Session runningSession = db.getSession(username); // get running session if there is one
            @Override
            public void onClick(View v)
            {
                if(runningSession.getSessionID() == 0) // if no running session
                {
                    showToast("You must start a session before using this function."); // inform user to start new session
                }
                else // if there is a running session
                {
                    Delivery unfinishedDelivery = db.getLastDelivery(runningSession); // check for unfinished delivery
                    if (!unfinishedDelivery.getDuration().equalsIgnoreCase("Not Completed")) // If delivery does not equal "Not Completed"
                    {
                        // begin camera function
                        Intent cIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (cIntent.resolveActivity(getActivity().getPackageManager()) != null)
                        {
                            startActivityForResult(cIntent, REQUEST_IMAGE_CAPTURE);
                        }
                    }
                    else // if there is a delivery Not Completed
                    {
                        showToast("You must complete previous delivery before starting a new one."); // inform user they have to complete a delivery first
                    }
                }
            }
        });

        //BUTTON FOR NAVIGATION USING GOOGLE MAPS
        btnNavigation.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                if (!deliveryPostcode.isEmpty()) // If global variable postcode is not empty
                {
                    // Build an AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Select your answer.");
                    builder.setMessage("Begin Navigation to " + deliveryPostcode + " ?");
                    // Set the alert dialog yes button click listener
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() // if user selects yes
                    {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            beginNavigation();
                            btnMarkDeliveryComplete.setVisibility(View.VISIBLE); // sets button as visible
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() // if user selects no
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
                else // if there is no postcode
                {
                    showToast("You must first capture a postcode \n" +
                            "before performing this action."); // inform user they must capture a postcode
                }

            }
        });

        //BUTTON FOR ADDING DELIVERY
        btnMarkDeliveryComplete.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v)
            {
                //Check permissions for fine and coarse location
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    //When permissions are not granted, request them.
                    ActivityCompat.requestPermissions(getActivity(), new String[]
                            {
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                            }, 100);
                }

                final DBHandler db = new DBHandler(getActivity()); // create instance of DBHandler
                final Session runningSession = db.getSession(username); // check for running session
                final Delivery lastDelivery = db.getLastDelivery(runningSession); // check for unfinished delivery

                    // Build an AlertDialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Select your answer.");
                    builder.setMessage("Completed Delivery to: " + lastDelivery.getAddress() + " ?");
                    // Set the alert dialog yes button click listener
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() // if yes selected
                    {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            try
                            {
                                db.updateDelivery(lastDelivery); // update delivery as complete
                                db.addDeliveryToSession(runningSession); // adds delivery to current running session
                                btnMarkDeliveryComplete.setVisibility(View.GONE); // hides button
                            }
                            catch (ParseException ex)
                            {
                                ex.printStackTrace();
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
        return view; // end of fragment view
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        displayImage = view.findViewById(R.id.imgDisplayImage);
        displayText = view.findViewById(R.id.txtDisplayText);
        displayText.setText("Use the Capture Image button to activate the camera." +
                "\n Ensure postcode is visible and clear before capturing.");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,  Intent data) // on activity result for camera function
    {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) // if request code and resultcode match
        {
            Bundle bundle = data.getExtras(); // get bundle from intent

            //Extract image from bundle
            Bitmap bitmap = (Bitmap) bundle.get("data");

            //set the image view to the image
            displayImage.setImageBitmap(bitmap);

            //process image
            //create the firebase image from bitmap
            FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

            //get instance of FirebaseVision
            FirebaseVision firebaseVision = FirebaseVision.getInstance();

            //create instance of FirebaseVisionTextRecognizer
            FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();

            //Create a task to process the image
            Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);

            //If the task succeeds
            task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>()
            {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText)
                {
                    processText(firebaseVisionText); // calls processText method and passes in firebaseVisionText object
                }
            });

            //If task fails
            task.addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    showToast(e.toString());
                }
            });
        }
    }

    //Method for processing firebase Vision text
    private void processText(FirebaseVisionText text)
    {
        displayText.setText(null); // set display text to null
        String pictureText = text.getText(); // gets text from pictureText

        if (text.getTextBlocks().size() == 0) // if text does not contain any text
        {
            showToast("No Postcode Detected"); // inform user there is no postcode
        }

        if(pictureText.contains("PA")) // checks captured text to check for characters "PA"
        {
            String target = "PAZ"; // target to replace
            String replacement = "PA2"; // replacement to replace target
            String newString = pictureText.replace(target, replacement); // new String that replaces target with replacement
            pictureText = newString; // replaces picture text with the newstring
        }
        else
        {
            displayText.setText(""); // set display text to empty or nothing
            String noPostcode = "No postcode detected. \n Please ensure postcode is visible and clear. \nTry again with Capture Image";
            displayText.append(noPostcode); // sets displayText to noPostcode message
        }
        //init variables
        String message = "";
        String postcode ="";
        String address = "";
        ArrayList<String> linesArr = new ArrayList<>(); // Creates a new array list of strings to hold each line of the captured image
        String keyword = "PA"; // creates a new string keyword as PA
        String[] lines = pictureText.split("\\r?\\n"); // puts each line of the captured image into the lines string array
        for(String line : lines) // for each line in the string array
        {
            if(!line.contains(keyword)) // if the line does not contain the keyword
            {
                linesArr.add(line); // add line to the array
            }
            else // if line does contain the keyword
            {
                address = linesArr.get(linesArr.size()-1); // get the address by getting the line before the postcode
                publicAddress = address; // assigns address to the public address global variable
                postcode = line; // postcode is equal to current line
                deliveryPostcode = postcode; // global postcode is equal to postcode from array
            }
            if (postcode.contains("PA")) // If postcode contains PA
            {
                String target = "PAZ"; // target to replace in string
                String replacement = "PA2"; // value to replace target
                String newString = postcode.replace(target, replacement); // replaces the target with the replacement string
                postcode = newString; // new string is equal to postcode
            }
            if (postcode.contains("PA")) // If postcode contains PA
            {
                String target = "PAl"; // target to replace in string
                String replacement = "PA1"; // value to replace target
                String newString = postcode.replace(target, replacement); // replaces the target with the replacement string
                postcode = newString; // new string is equal to postcode
            }
        }
        //creates string of address and postcode
        message = address + "\n" +
                  postcode + "\n";

        displayText.append(message); // appends message to displayText textview
    }

    //NAVIGATION USING GOOGLE MAPS METHOD
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void beginNavigation()
    {
        final Geocoder geocoder = new Geocoder(this.getContext()); // create new instance of geocoder
        if (displayText != null) // if display text does not equal null
        {
            DBHandler db = new DBHandler(getActivity()); // create new instance of dbhandler
            Delivery newDelivery = new Delivery(); // new delivery object
            List<String> lines = new ArrayList<>(); // new array list of strings
            int count = displayText.getLineCount(); // sets new int variable as the number of lines in the displaytext
            String postcode = ""; // init
            String address = ""; // init
            String latAndLong = ""; // init
            double lat = 0.0; // init
            double lon = 0.0; // init
            for (int line = 0; line < count; line++) // for loop that iterates through every line in the array
            {
                int start = displayText.getLayout().getLineStart(line); // sets int for start of array
                int end = displayText.getLayout().getLineEnd(line); // sets int for end of the array
                CharSequence substring = displayText.getText().subSequence(start, end); // substring is the text between the two variables start and end.
                lines.add(substring.toString()); // adds substring to array list lines
                if (substring.toString().contains("PA")) // if substring contains PA
                {
                    String target = "PAZ"; // target to replace in string
                    String replacement = "PA2"; // value to replace target
                    String newString = substring.toString().replace(target, replacement); // replaces the target with the replacement string
                    postcode = newString; // new string is equal to postcode
                    address = lines.get(line-1); // address is equal to the line before postcode line
                }
                if (substring.toString().contains("PA")) // if substring contains PA
                {
                    String target = "PAl"; // target to replace in string
                    String replacement = "PA1"; // value to replace target
                    String newString = substring.toString().replace(target, replacement); // replaces the target with the replacement string
                    postcode = newString; // new string is equal to postcode
                    address = lines.get(line-1); // address is equal to the line before postcode line
                }
                Session currentSession = db.getSession(username); // assigns current session to session returned by db getSession method

                getLocation(); // runs method to get phones location
                double distance = getDistance(phoneLat, phoneLon, deliveryLat, deliveryLon); // runs method to return distance between phones location and delivery location

                newDelivery.setSessionID(currentSession.getSessionID()); // sets sessionId as current SessionId to new delivery object
                newDelivery.setStartTime(LocalTime.now().truncatedTo(ChronoUnit.MINUTES)); // sets current time as start time for new delivery object
                newDelivery.setDeliveredTime(LocalTime.now().truncatedTo(ChronoUnit.MINUTES)); // sets current time as completed time for new delivery object
                newDelivery.setDuration("Not Completed"); // Sets delivery objects duration to "Not Completed"
                newDelivery.setAddress(publicAddress + ", " + deliveryPostcode); // sets address to address and postcode
                newDelivery.setDistance(String.valueOf(distance)); // dets distance to distance between phone and delivery address

                try
                {
                    List<Address> addresses = geocoder.getFromLocationName(postcode, 1); // gets address from postcode using geocoder
                    if (addresses != null && !addresses.isEmpty()) // if address does not equal null and not empty
                    {
                        Address capturedAddress = addresses.get(0); // get first address from array
                        lat = capturedAddress.getLatitude(); // sets latitude to lat variable
                        lon = capturedAddress.getLongitude(); // sets longitude to lon variable
                        deliveryLat = lat; // sets global lat variable to lat acquired above
                        deliveryLon = lon; // sets global lon variable to lon acquired above
                       // latAndLong = "Lat: " + lat + "\n" + "Long: " + lon + "\n" + address;
                    }
                }
                catch (Exception ex) // catches try exception
                {
                    Log.d("EXCEPTION", ex.toString());
                }
            }
            db.addNewDelivery(newDelivery); // adds delivery object to database
            Uri addressURI = Uri.parse("geo:"+lat+","+lon+"?q="+address); // Creates uri address for passing to google maps
            Intent intent = new Intent(Intent.ACTION_VIEW,(addressURI)); // creates intent for starting google maps activity
            intent.setPackage("com.google.android.apps.maps"); // sets the name of the application to open
            startActivity(intent); // starts the google maps activity specified above
        }
        else
        {
            showToast("No picture detected"); // informs the user there is no picture
        }
    }

    //METHOD TO GET CURRENT LOCATION
    @SuppressLint("MissingPermission")
    private void getLocation()
    {
        try
        {
            //Initialise location manager
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            //check permissions
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER))
            {
                //get last location
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Location> task)
                    {
                        //Initialise location
                        Location location = task.getResult();
                        //check if not null
                        if (location != null)
                        {
                            //set lat and long to locations lat and long
                            phoneLat = location.getLatitude();
                            phoneLon = location.getLongitude();
                        }
                        else
                        //request location
                        {
                            LocationRequest locationRequest = new LocationRequest()
                                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                    .setInterval(10000)
                                    .setFastestInterval(1000)
                                    .setNumUpdates(1);
                            //Initialise callback
                            LocationCallback locationCallback = new LocationCallback()
                            {
                                @Override
                                public void onLocationResult(LocationResult locationResult)
                                {
                                    //initialise new location
                                    Location location1 = locationResult.getLastLocation();
                                    phoneLat = location1.getLatitude();
                                    phoneLon = location1.getLongitude();
                                }
                            };
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        }
                        Log.d("PHONE LAT", String.valueOf(phoneLat));
                    }
                });
            }
            else
            {
                //When location service is not enabled, open location setting
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
        catch(Exception ex)
        {
            Log.d("GET LOCATION", ex.toString());
        }
    }

    //METHOD THAT RUNS WHEN USER GRANTS PERMISSIONS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        // Check results
        if (requestCode == 100 && grantResults.length >0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED))
        {
            //If permission granted, call method.
            getLocation();
        }
        else
        {
            showToast("GPS Permissions are required for this application to function.");
        }
    }

    //METHOD TO GET DISTANCE BETWEEN PHONE AND DELIVERY ADDRESS
    private double getDistance(double lat1, double lon1, double lat2, double lon2)
    {
        //creates a new location object and sets it to the lat and long of the passed values
        Location location1 = new Location("location1");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);

        //creates a new location object and sets it to the lat and long of the passed values
        Location location2 = new Location("location2");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);

        double distance = location1.distanceTo(location2) / 1000;
        double roundedDistance = Math.round(distance * 10) / 10.0;
        return roundedDistance;
    }


    //Toast method
    private void showToast(String message)
    {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    //Methods generated by implemented locationlistener
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
    public void onLocationChanged(Location location) {}
}
