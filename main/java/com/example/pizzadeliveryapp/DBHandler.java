package com.example.pizzadeliveryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.LocaleList;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

//----------------------------------------------- CREATING DATABASE -----------------------------------------------------------------

public class DBHandler extends SQLiteOpenHelper {
    //Database Info
    public static final String DATABASE_NAME = "PizzaHut.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_SESSION = "SESSION_TABLE";
    public static final String TABLE_USER = "USER_TABLE";
    public static final String TABLE_DELIVERY = "DELIVERY_TABLE";
    //Session columns
    public static final String COLUMN_SESSION_ID = "SessionId";
    public static final String COLUMN_SESSION_ACTIVE = "ActiveSession";
    public static final String COLUMN_SESSION_DATE = "Date";
    public static final String COLUMN_SESSION_START = "StartTime";
    public static final String COLUMN_SESSION_END = "EndTime";
    public static final String COLUMN_SESSION_DELIVERIES = "Deliveries";
    public static final String COLUMN_SESSION_USER_ID = "UserId";
    //User Columns
    public static final String COLUMN_USER_ID = "UserId";
    public static final String COLUMN_USER_USERNAME = "Username";
    public static final String COLUMN_USER_PASSWORD = "Password";
    public static final String COLUMN_USER_EMAIL = "EmailAddress";
    //Delivery Columns
    public static final String COLUMN_DELIVERY_ID = "DeliveryId";
    public static final String COLUMN_DELIVERY_SESSION_ID = "SessionId";
    public static final String COLUMN_DELIVERY_START_TIME = "StartTime";
    public static final String COLUMN_DELIVERY_COMPLETED_TIME = "CompletedTime";
    public static final String COLUMN_DELIVERY_DURATION = "Duration";
    public static final String COLUMN_DELIVERY_ADDRESS = "Address";
    public static final String COLUMN_DELIVERY_DISTANCE = "Distance";


    //Initialise the database, constructors
    public DBHandler(Context context) { super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    //this is called the first time a database is accessed.
    public void onCreate(SQLiteDatabase db)
    {
        // Creates string that will create a table if it does not exist
        String createSessionTable = "CREATE TABLE IF NOT EXISTS "
                + TABLE_SESSION + " (" + COLUMN_SESSION_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_SESSION_ACTIVE + " TEXT,"
                + COLUMN_SESSION_DATE + " TEXT,"
                + COLUMN_SESSION_START + " TEXT,"
                + COLUMN_SESSION_END + " TEXT,"
                + COLUMN_SESSION_DELIVERIES + " TEXT,"
                + COLUMN_SESSION_USER_ID + " TEXT)";

        // Creates string that will create a table if it does not exist
        String createUserTable = "CREATE TABLE IF NOT EXISTS "
                + TABLE_USER + " (" + COLUMN_USER_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_USERNAME + " TEXT,"
                + COLUMN_USER_PASSWORD + " TEXT,"
                + COLUMN_USER_EMAIL + " TEXT)";

        // Creates string that will create a table if it does not exist
        String createDeliveryTable = "CREATE TABLE IF NOT EXISTS "
                + TABLE_DELIVERY + " (" + COLUMN_DELIVERY_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DELIVERY_SESSION_ID + " TEXT,"
                + COLUMN_DELIVERY_START_TIME + " TEXT,"
                + COLUMN_DELIVERY_COMPLETED_TIME + " TEXT,"
                + COLUMN_DELIVERY_DURATION + " TEXT,"
                + COLUMN_DELIVERY_ADDRESS + " TEXT,"
                + COLUMN_DELIVERY_DISTANCE + " TEXT)";

        // Executes the create table strings specified above
        db.execSQL(createSessionTable);
        db.execSQL(createUserTable);
        db.execSQL(createDeliveryTable);
    }

    @Override
    //This is called if the database version number changes.
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELIVERY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
        onCreate(db);
    }
    //----------------------------------------------- END OF CREATING DATABASE -----------------------------------------------------------------

    //----------------------------------------------- SESSION FUNCTIONS -----------------------------------------------------------------

    public boolean addSession(Session session) // Adds session instance to the db
    {
        ContentValues cv = new ContentValues(); // New Content Values instance

        // Assigns values to the content value object that will be entered into the database
        cv.put(COLUMN_SESSION_ACTIVE, session.getActive());
        cv.put(COLUMN_SESSION_DATE, session.getDate());
        cv.put(COLUMN_SESSION_START, session.getStartTime().toString());
        cv.put(COLUMN_SESSION_END, session.getEndTime().toString());
        cv.put(COLUMN_SESSION_DELIVERIES, session.getDeliveries());
        cv.put(COLUMN_SESSION_USER_ID, session.getUserId());

        SQLiteDatabase db = DBHandler.this.getWritableDatabase(); // get's a version of the db that can be written to
        // Creates boolean that is true if write operation is successful, or negative if unsuccessful
        boolean registerSuccess = db.insert(TABLE_SESSION, null, cv) > 0;
        db.close(); // closes db connection
        if (!registerSuccess) // If boolean is false
        {
            return false;
        }
        else // if boolean is true
        {
            return true;
        }
    }

    public void addDeliveryToSession(Session sessionIn) // Method to add a delivery to the specified session
    {
        SQLiteDatabase db = getWritableDatabase(); // New instance of writeable database
        ContentValues cv = new ContentValues(); // new instance of Content Values
        int newDeliveryAmount = sessionIn.getDeliveries() + 1; // Adds one to the number of deliveries the session passed in parameters has

        cv.put(COLUMN_SESSION_DELIVERIES,  newDeliveryAmount); // Puts the column and value to update the db with into the cv object.
        String whereClause = COLUMN_SESSION_ID + "=?"; // Specifies the entry to update based on the session id
        String whereArgs[] = {String.valueOf(sessionIn.getSessionID())}; // gets the session id from the session parameter
        db.update(TABLE_SESSION, cv, whereClause, whereArgs); // updates the table using the information specified
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<Session> getAllSessions(User userIn) // method to get all sessions for the passed in user
    {
        ArrayList<Session> allSessions = new ArrayList<Session>(); // new array list to hold all sessions
        int userId = userIn.getUserId(); // gets user id from the passed in user

        // selects all entries from session table where user id is the passed in user id
        String selectedQuery = "SELECT * FROM " + TABLE_SESSION + " WHERE UserId=" + userId;

        SQLiteDatabase db = this.getReadableDatabase(); // get readable database
        try { // first try
            Cursor cursor = db.rawQuery(selectedQuery, null); // creates a new cursor instance to move through the results returned by querying the db
            try { // second try
                //loop through all rows
                if (cursor.moveToFirst()) { // if cursor returns data
                    do {
                        Session session = new Session(); // new session
                        session.setSessionID(cursor.getInt(0)); // sets new sessions id as the value retrieved in column index position 0
                        session.setActive(cursor.getInt(1)); // sets new sessions active variable as the value retrieved in column index position 1
                        session.setDate(cursor.getString(2)); // sets new sessions date as the value retrieved in column index position 2

                        String stringStartTime = cursor.getString(3); // sets string start time as the value retrieved in column index position 3
                        LocalTime localStartTime = LocalTime.parse(stringStartTime); // converts string start time to Local Time
                        session.setStartTime(localStartTime); // sets new session start time as the local time start time

                        // Same process as start time
                        String stringEndTime = cursor.getString(4);
                        LocalTime localEndTime = LocalTime.parse(stringEndTime);
                        session.setStartTime(localEndTime);

                        session.setDeliveries(cursor.getInt(5)); // sets new sessions deliveries as the value retrieved in column index position 5
                        session.setUserId(cursor.getInt(6)); // sets new sessions user Id as the value retrieved in column index position 6

                        allSessions.add(session); // adds the new session to the array list all sessions
                    }
                    while (cursor.moveToNext()); // Adds a new session to the array list every time the cursor recieves information after moving to the next entry
                }
            } // end of second try
            finally
            {
                try
                {
                    cursor.close(); // closes cursor object.
                }
                catch (Exception ex) {}
            }
        } // end of first try
        finally
        {
            try
            {
                db.close(); // closes db connection
            }
            catch (Exception ignore) {}
        }
        if (allSessions.size() > 0)
        {
            Log.d("Checking Deliveries", allSessions.get(0).getDate());
        }
        return allSessions; // returns allSessions which will contain all sessions in the db for the specified user/
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Session getSession(String usernameIn) // method for returning a users current session
    {
        User user = getUser(usernameIn); // User is the user passed in
        SQLiteDatabase db = this.getReadableDatabase(); // get readable database to view information
        Session session = new Session(); // new session instance
        String userIdString = String.valueOf(user.getUserId()); // String variable to hold the user id of passed in user

        String[] columns = {COLUMN_SESSION_ID, COLUMN_SESSION_ACTIVE, COLUMN_SESSION_DATE, COLUMN_SESSION_START,
                COLUMN_SESSION_END, COLUMN_SESSION_DELIVERIES, COLUMN_SESSION_USER_ID}; // columns to return info from
        String selection = COLUMN_SESSION_ACTIVE + " = ?" + " AND " + COLUMN_SESSION_USER_ID + " = ?"; // columns for the where clause
        String[] selectionArgs = {"1", userIdString}; // the values passed in for the where clause
        Cursor cursor = db.query(TABLE_SESSION,        //Table to query
                columns,                            //columns to return
                selection,                          //columns for the WHERE clause
                selectionArgs,                      //The values for the WHERE clause
                null,
                null,
                null);

        if (cursor != null) // if cursor is not null
        {
            while (cursor.moveToNext()) // cursor moves to next value
            {
                //assign values to variables based on results from the db
                int sessionId = cursor.getColumnIndex(COLUMN_SESSION_ID);
                int sessionActive = cursor.getColumnIndex(COLUMN_SESSION_ACTIVE);
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_DATE));

                String startTime = cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_START));
                LocalTime startTimeLocal = LocalTime.parse(startTime);

                String endTime = cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_END));
                LocalTime endTimeLocal = LocalTime.parse(endTime);

                String deliveries = cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_DELIVERIES));
                int deliveriesAsInt = Integer.valueOf(deliveries);

                int userId = cursor.getColumnIndex(COLUMN_SESSION_USER_ID);

                //assigns the variables declared above to the new session object.
                session = new Session(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_SESSION_ID)),
                        sessionActive,
                        date,
                        startTimeLocal,
                        endTimeLocal,
                        deliveriesAsInt,
                        user.getUserId()
                );
                cursor.close(); // close cursor
            } // end of while loop
            return session; // returns the session object
        } // if cursor is null
        return null; // return null
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void endSession(Session sessionIn) // ends the session passed in
    {
        SQLiteDatabase db = getWritableDatabase(); // get writeable database
        ContentValues cv = new ContentValues(); // Creates new content value object.
        cv.put(COLUMN_SESSION_ACTIVE, 0); // In the column "active", sets the value to 0, which is equal to false
        cv.put(COLUMN_SESSION_END, LocalTime.now().truncatedTo(ChronoUnit.MINUTES).toString()); // Updates the end time column with the time the method is called.
        String whereClause = COLUMN_SESSION_ID + "=?"; // specifies the where clause
        String whereArgs[] = {String.valueOf(sessionIn.getSessionID())}; // specifies the where argument
        db.update(TABLE_SESSION, cv, whereClause, whereArgs); // runs the update function passing in the values specified.
    }

    //----------------------------------------------------------- END OF SESSION FUNCTIONS ------------------------------------------------------------

    //----------------------------------------------------------- USER FUNCTIONS ----------------------------------------------------------------------
    public boolean addUser(User user) // Adds user to db returning true or false depending on success
    {
        ContentValues cv = new ContentValues(); // new content value object

        //Columns and values to add info to are added to content value object
        cv.put(COLUMN_USER_USERNAME, user.getUsername());
        cv.put(COLUMN_USER_PASSWORD, user.getPassword());
        cv.put(COLUMN_USER_EMAIL, user.getEmailAddress());

        SQLiteDatabase db = DBHandler.this.getWritableDatabase(); // get writeable database.

        // values are added to the database and boolean object is created with a value true if the action is successful, or false if not.
        boolean registerSuccess = db.insert(TABLE_USER, null, cv) > 0;
        db.close(); // close the db connection
        if (!registerSuccess) // If not true
        {
            return false; // return false
        }
        else // if true
        {
            return true; // return true
        }
    }

    public boolean getEmail(String emailIn) // Method to check if the passed email exists
    {
        boolean emailExists = false; // create boolean variable and initialise as false
        SQLiteDatabase db = this.getReadableDatabase(); // get readable db

        String[] columns = {COLUMN_USER_EMAIL}; // Specify column to get data from
        String selection = COLUMN_USER_EMAIL + " = ?"; // column to use where clause on
        String[] selectionArgs = {emailIn}; // value to pass in to where clause
        Cursor cursor = db.query(TABLE_USER,        //Table to query
                columns,                            //columns to return
                selection,                          //columns for the WHERE clause
                selectionArgs,                      //The values for the WHERE clause
                null,
                null,
                null);

        int cursorCount = cursor.getCount(); // gets cursor count
        cursor.close(); // closes cursor
        db.close(); // closes db
        if (cursorCount > 0) // if cursor count above 0, results have been returned
        {
            emailExists = true; // if results are above 0, email already exists in db, sets variable to true
        }
        return emailExists; // if no results are found, email does not exist, variable is treturned as false.

    }

    public String getUsername(String emailIn) // method to get username taking in email as parameter
    {
        String username = ""; // Initialise username variable
        SQLiteDatabase db = this.getReadableDatabase(); // get readable db instance

        String[] columns = {COLUMN_USER_USERNAME}; // Column to query
        String selection = COLUMN_USER_EMAIL + " = ?"; // column to use where clause
        String[] selectionArgs = {emailIn}; // value to pass in to where clause
        Cursor cursor = db.query(TABLE_USER,        //Table to query
                columns,                            //columns to return
                selection,                          //columns for the WHERE clause
                selectionArgs,                      //The values for the WHERE clause
                null,
                null,
                null);

        if (cursor != null) // if cursor is not null
        {
            while (cursor.moveToNext())
            {
                // move the cursor to next row if there is any to read it's data
                username = cursor.getString(cursor.getColumnIndex(COLUMN_USER_USERNAME));
            }
        }
        return username; // returns username variable
    }

    public User getUser(String usernameIn) // method that gets specific user
    {
        User user = new User(); // new instance of user
        SQLiteDatabase db = this.getReadableDatabase(); // gets readable db

        String[] columns = {COLUMN_USER_ID, COLUMN_USER_USERNAME, COLUMN_USER_PASSWORD, COLUMN_USER_EMAIL}; // colums to return information from
        String selection = COLUMN_USER_USERNAME + " = ?"; // column to target with where clause
        String[] selectionArgs = {usernameIn}; // information that where clause will use
        Cursor cursor = db.query(TABLE_USER,        //Table to query
                columns,                            //columns to return
                selection,                          //columns for the WHERE clause
                selectionArgs,                      //The values for the WHERE clause
                null,
                null,
                null);

        if (cursor != null) // if cursor is not null
        {
            cursor.moveToFirst(); // move to first info returned

            //assigns values returned to user instance
            user = new User(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_USER_EMAIL))
            );
            cursor.close(); // close cursor

            return user; // returns user instance
        }
        return user; // returns user instance
    }

    public boolean userLookup(String emailIn, String passwordIn) // Method that checks for users in the db and returns true or false
    {
        // array of columns to fetch
        String[] columns = { COLUMN_USER_EMAIL, COLUMN_USER_PASSWORD };
        SQLiteDatabase db = this.getReadableDatabase();
        // selection criteria
        String selection = COLUMN_USER_EMAIL + " = ?" + " AND " + COLUMN_USER_PASSWORD + " = ?";
        // selection argument
        String[] selectionArgs = {emailIn, passwordIn};
        // query user table with condition
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,                    //columns to return
                selection,                  //columns for the WHERE clause
                selectionArgs,              //The values for the WHERE clause
                null,
                null,
                null);

        int cursorCount = cursor.getCount(); // gets number of values returned
        cursor.close(); // closes cursor
        db.close(); // closes db connection
        if (cursorCount > 0) // if values exist
        {
            return true; // return true, indicating user exists
        }
        return false; // if no values exist, return false as user does not exist
    }

    //----------------------------------------------------------- END OF USER FUNCTIONS --------------------------------------------------------------------

    //----------------------------------------------------------- START OF DELIVERY FUNCTIONS --------------------------------------------------------------------

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean addNewDelivery(Delivery deliveryIn) // method for adding new delivery to delivery table
    {
        ContentValues cv = new ContentValues(); // new content values object

        //Assign content values object the column to add info to, and the value to put in
        cv.put(COLUMN_DELIVERY_SESSION_ID, deliveryIn.getSessionID());
        cv.put(COLUMN_DELIVERY_START_TIME, deliveryIn.getStartTime().toString());
        cv.put(COLUMN_DELIVERY_COMPLETED_TIME, deliveryIn.getDeliveredTime().toString());
        cv.put(COLUMN_DELIVERY_DURATION, deliveryIn.getDuration());
        cv.put(COLUMN_DELIVERY_ADDRESS, deliveryIn.getAddress());
        cv.put(COLUMN_DELIVERY_DISTANCE, deliveryIn.getDistance());

        SQLiteDatabase db = DBHandler.this.getWritableDatabase(); // get writeable db as the information is being added
        boolean registerSuccess = db.insert(TABLE_DELIVERY, null, cv) > 0; // creates boolean variable to determine if insert worked correctly
        db.close(); // close db connection
        if (!registerSuccess) // if insert did not work
        {
            return false; // return false
        }
        else // if insert worked correctly
        {
            return true; // return true
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Delivery getLastDelivery(Session sessionIn) // get's the last delivery added to current session passed in parameters
        {
            SQLiteDatabase db = this.getReadableDatabase(); // get readable db
            Delivery delivery = new Delivery(); // create new delivery instance
            String sessionIdString = String.valueOf(sessionIn.getSessionID()); // get session id from passed in session

            String[] columns = {COLUMN_DELIVERY_ID, COLUMN_DELIVERY_SESSION_ID, COLUMN_DELIVERY_START_TIME, COLUMN_DELIVERY_COMPLETED_TIME,
                    COLUMN_DELIVERY_DURATION, COLUMN_DELIVERY_ADDRESS, COLUMN_DELIVERY_DISTANCE}; // columns to return info from
            String selection = COLUMN_DELIVERY_SESSION_ID + " = ?"  + " AND " + COLUMN_DELIVERY_DURATION + " = ?"; // columns to target with where clause
            String[] selectionArgs = {sessionIdString, "Not Completed"}; // values to be used in where clause
            Cursor cursor = db.query(TABLE_DELIVERY,        //Table to query
                    columns,                            //columns to return
                    selection,                          //columns for the WHERE clause
                    selectionArgs,                      //The values for the WHERE clause
                    null,
                    null,
                    null);

            if (cursor != null) // if cursor not null
            {
                while (cursor.moveToNext()) // cursor moves to next value
                {
                    //create variables to hold the value returned from columns by cursor
                    int deliverySessionId = cursor.getColumnIndex(COLUMN_DELIVERY_SESSION_ID);

                    String startTime = cursor.getString(cursor.getColumnIndex(COLUMN_DELIVERY_START_TIME));
                    LocalTime startTimeLocal = LocalTime.parse(startTime);

                    String endTime = cursor.getString(cursor.getColumnIndex(COLUMN_DELIVERY_COMPLETED_TIME));
                    LocalTime endTimeLocal = LocalTime.parse(endTime);

                    String duration = cursor.getString(cursor.getColumnIndex(COLUMN_DELIVERY_DURATION));
                    String address = cursor.getString(cursor.getColumnIndex(COLUMN_DELIVERY_ADDRESS));
                    String distance = cursor.getString(cursor.getColumnIndex(COLUMN_DELIVERY_DISTANCE));

                    // assign the variables the received cursor information to the appropriate field for delivery object
                    delivery = new Delivery(
                            cursor.getInt(cursor.getColumnIndex(COLUMN_DELIVERY_ID)),
                            deliverySessionId,
                            startTimeLocal,
                            endTimeLocal,
                            duration,
                            address,
                            distance
                    );
                }
                cursor.close(); // closes cursor
                return delivery; // return delivery object
            }
            return null; // returns null if there is no  delivery
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public ArrayList<Delivery> getAllDeliveries(Session sessionIn) // returns array list of deliveries for the passed in session
        {
            ArrayList<Delivery> allDeliveries = new ArrayList<Delivery>(); // creates new array list of deliveries.
            String sessionIdString = String.valueOf(sessionIn.getSessionID()); // gets session id from passed in session

            String selectedQuery = "SELECT * FROM " + TABLE_DELIVERY + " WHERE SessionId=" + sessionIdString; // Select all query where session id is the passed in sessions session id

            SQLiteDatabase db = this.getReadableDatabase();// get readable db
            try {
                Cursor cursor = db.rawQuery(selectedQuery, null);
                try {
                    //loop through all rows
                    if (cursor.moveToFirst()) {
                        do {
                            Delivery delivery = new Delivery(); // new delivery object
                            // assign values returned from cursor to the delivery object
                            delivery.setDeliveryID(cursor.getInt(0));
                            delivery.setSessionID(cursor.getInt(1));

                            String stringStartTime = cursor.getString(2);
                            LocalTime localStartTime = LocalTime.parse(stringStartTime);
                            delivery.setStartTime(localStartTime);

                            String stringEndTime = cursor.getString(3);
                            LocalTime localEndTime = LocalTime.parse(stringEndTime);
                            delivery.setDeliveredTime(localEndTime);

                            delivery.setDuration(cursor.getString(4));
                            delivery.setAddress(cursor.getString(5));
                            delivery.setDistance(cursor.getString(6));

                            allDeliveries.add(delivery); // add delivery object to array list
                        }
                        while (cursor.moveToNext()); // cursor moves to next row
                    }
                }
                finally
                {
                    try
                    {
                        cursor.close(); // close cursor
                    }
                    catch (Exception ex) {}
                }
            }
            finally
            {
                try
                {
                    db.close(); // close db connection
                }
                catch (Exception ignore) {}
            }
            return allDeliveries; // returns array list containing all deliveries
        }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateDelivery(Delivery deliveryIn) throws ParseException // Method that updates deliveries in the db
    {
        SQLiteDatabase db = getWritableDatabase(); // get writeable db
        ContentValues cv = new ContentValues(); // new content value object

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); // New simple date format object with the pattern HH-mm for hours and minutes

        String deliveryStart = deliveryIn.getStartTime().toString(); // gets delivery start time from passed in delivery
        Date startDateTime = sdf.parse(deliveryStart); // converts start time into the format specified in the simple date format

        String deliveryCompleted = LocalTime.now().truncatedTo(ChronoUnit.MINUTES).toString(); // sets delivery completed time to current time
        Date endDateTime = sdf.parse(deliveryCompleted); // converts completed time using the simple date format

        long difference = endDateTime.getTime() - startDateTime.getTime(); // get's the difference between the start time and end time to work out duration
        if(difference < 0) // checks if difference is in negative values
        {
            Date dateMax = sdf.parse("24:00");
            Date dateMin = sdf.parse("00:00");
            //ensures difference is in the positive range
            difference=(dateMax.getTime() - startDateTime.getTime()) + (endDateTime.getTime()-dateMin.getTime());
        }
        // Ensuring difference is in minutes
        int days = (int) (difference / (1000*60*60*24));
        int hours = (int) ((difference - (1000*60*60*24*days)) / (1000*60*60));
        int min = (int) (difference - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60);
        difference = min; // sets minutes for difference


        cv.put(COLUMN_DELIVERY_COMPLETED_TIME,  LocalTime.now().truncatedTo(ChronoUnit.MINUTES).toString()); // assign column to target and value to enter for cv object
        cv.put(COLUMN_DELIVERY_DURATION,  String.valueOf(difference)); // assign column to target and value to enter for cv object
        String whereClause = COLUMN_DELIVERY_ID + "=?" + " AND " + COLUMN_DELIVERY_DURATION + " =?"; // columns to target for where clause
        String whereArgs[] = {String.valueOf(deliveryIn.getDeliveryID()), "Not Completed"}; // values where clause will use
        db.update(TABLE_DELIVERY, cv, whereClause, whereArgs); // updates table based on data above.
    }



    //----------------------------------------------------------- END OF DELIVERY FUNCTIONS --------------------------------------------------------------------
}
