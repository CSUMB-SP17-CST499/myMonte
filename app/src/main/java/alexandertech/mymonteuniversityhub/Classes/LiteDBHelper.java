package alexandertech.mymonteuniversityhub.Classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.net.URLEncoder;

import static java.security.AccessController.getContext;

/**
 * Created by JAlexander on 3/30/2017.
 */
public class LiteDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "alexandertech.mymonteuniversityhub.session.db";
    private SQLiteDatabase db;
    public static final String COL_TASK_TITLE = "Title";
    public  SQLiteDatabase myDB;
    public String databasePath = "";
    Context context2;
    public static final String CAMPUSBUILDINGS_TABLE = "campusBuildings";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_BUILDINGNAME = "buildingName";
    public static final String COLUMN_GPSCOORDINATES = "gpsCoordinates";


    public LiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        databasePath = context.getDatabasePath(DATABASE_NAME).toString();
        this.context2 = context;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        //SQLiteDatabase myDB = this.getWritableDatabase();
        String CREATE_SESSION_TABLE = "CREATE TABLE IF NOT EXISTS  ActiveSessions ( id INTEGER PRIMARY KEY AUTOINCREMENT, FName VARCHAR , Type VARCHAR, LName VARCHAR, Email VARCHAR, SessionKey VARCHAR, DatabaseID VARCHAR, Date VARCHAR);";
        db.execSQL(CREATE_SESSION_TABLE);

    }




    //method for storing the account when the user log in. 
    public boolean storeAccount (String FName, String LName, String Email, String SessionKey, String DatabaseID ){
        boolean isSaved=false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        SQLiteDatabase myDB = this.getWritableDatabase();
            String statement = "INSERT INTO ActiveSessions (FName, LName, Email, SessionKey, DatabaseID, Date) VALUES ('" +FName+"' , '"+LName+"', '"+Email+"' , '"+SessionKey+"' , '"+DatabaseID+"' , " +date+ ")";
            myDB.execSQL(statement);
        String CheckInsert = "SELECT * FROM ActiveSessions WHERE SessionKey = '"+SessionKey+"';";
        Cursor cursor = myDB.rawQuery(CheckInsert, null);
        if (cursor.moveToFirst()) {
            isSaved = true;
        } else {
            isSaved = false;
        }
        return isSaved;
    }
   //method to qwuery the DB to check to see if there is an active/valid session 
public boolean getUserLoginStatus(){
    boolean session=false;
    SQLiteDatabase myDB = this.getWritableDatabase();
    String CheckIfSessionExists = "SELECT * FROM ActiveSessions";
    Cursor cursor = myDB.rawQuery(CheckIfSessionExists, null);

    if (cursor.moveToFirst()) {
        session = true;
    } else {
        session = false;
    }
    return session;
}




    //Getter, to get the Moodle User database ID from the local SQLite db
    public String getID(){
        SQLiteDatabase myDB = this.getWritableDatabase();
        String dbID = "SELECT DatabaseID FROM ActiveSessions";
        Cursor cursor = myDB.rawQuery(dbID, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return " ";
    }
    //method to get the first name of the user 
    public String getFName(){
        SQLiteDatabase myDB = this.getWritableDatabase();
        String FName = "SELECT FName FROM ActiveSessions";
        Cursor cursor = myDB.rawQuery(FName, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return " ";
    }
    //method to get the last name of the user
    public String getLName(){
        SQLiteDatabase myDB = this.getWritableDatabase();
        String LName = "SELECT LName FROM ActiveSessions";
        Cursor cursor = myDB.rawQuery(LName, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return " ";
    }
    //method to get the email of the user
    public String getEmail(){
        SQLiteDatabase myDB = this.getWritableDatabase();
        String Email = "SELECT Email FROM ActiveSessions";
        Cursor cursor = myDB.rawQuery(Email, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return " ";
    }
    //method to get the sessionkey from the user
    public String getSessionKey(){
        SQLiteDatabase myDB = this.getWritableDatabase();
        String SeshKey = "SELECT SessionKey FROM ActiveSessions";
        Cursor cursor = myDB.rawQuery(SeshKey, null);
        if (cursor.moveToFirst()) {

            return cursor.getString(0);
        }
        return " ";
    }

    //Need to addmethod Boolean CheckSessionDate, that queries the db, get the insert date and make sure it's not 2 weeks (14 days old) otherwise we grab the session ID and run the delete method. 
    //Session is now invalid
    
    //method to log the user out. 
public boolean logout(String SESSION) throws IOException {
    Boolean isDeleted;
    SQLiteDatabase myDB = this.getWritableDatabase();
    String statement = "DELETE FROM ActiveSessions";
    myDB.execSQL(statement);
    String CheckDelete = "SELECT * FROM ActiveSessions";
    Cursor cursor = myDB.rawQuery(CheckDelete, null);
    if (cursor.moveToFirst()) {
        isDeleted = false;
    } else {
        isDeleted = true;
    }
    myDB.close();
    return isDeleted;
}

 public void clearSessionFromRemoteDB(String ID) throws IOException {
     String urlParameters = "Task=clearSession&DeviceID="+ID;
     URL url = new URL("https://monteapp.me/moodle/monteapi/authn/sessionInsert.php?" + urlParameters);
     System.out.println(url.toString());
     HttpURLConnection connection = null;
     connection = (HttpURLConnection) url.openConnection();
     connection.setDoInput(true);
     connection.setDoOutput(true);
     connection.setInstanceFollowRedirects(false);
     connection.setRequestMethod("GET");
     connection.connect();
     System.out.println(connection.getResponseCode());
 }
 public boolean checkRemoteSession(String AndroidFCMID) throws IOException, JSONException {
    boolean session  = false;
     URL url  = new URL("https://monteapp.me/moodle/monteapi/authn/SessionDisable.php?task=checkSession&DeviceID="+AndroidFCMID);
     HttpURLConnection connection = null;
     connection = (HttpURLConnection) url.openConnection();
     connection.setDoInput(true);
     connection.setDoOutput(true);
     connection.setInstanceFollowRedirects(false);
     connection.setRequestMethod("GET");
     connection.connect();
     DataOutputStream printout;
     printout = new DataOutputStream(connection.getOutputStream());
     JSONObject json = new JSONObject();
     printout.writeBytes((json.toString()));
     printout.flush();
     printout.close();
     BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
     StringBuilder sb = new StringBuilder();
     String line;
     while ((line = br.readLine()) != null) {
         String someline = "[ " + line.toString() + "]";
         JSONArray jArray = new JSONArray(someline);
         for (int i =0; i<jArray.length(); i++){
         String jsonSession = jArray.getJSONObject(i).getString("Session");
             if (jsonSession.equals("Active")){
                 return true;
             }
             else{
                 return false;
             }
         }
     }
     return false;
 }
 public void insertSessionIntoRemoteDB (String remoteDbId, String FName, String LName, String AndroidFCMID) throws IOException {
     String urlParameters = "Task=newUser&FName=" +FName+ "&LName="+LName+"&remoteDBId="+remoteDbId+"&DeviceID="+AndroidFCMID;
     URL url = new URL("https://monteapp.me/moodle/monteapi/authn/sessionInsert.php?" + urlParameters);

     HttpURLConnection connection = null;
         connection = (HttpURLConnection) url.openConnection();
         connection.setDoInput(true);
         connection.setDoOutput(true);
         connection.setInstanceFollowRedirects(false);
         connection.setRequestMethod("GET");
         connection.connect();
         System.out.println(connection.getResponseCode());

 }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int insertTask(String task_title, String mdl_db_id, String due_date, String android_reg_token) throws IOException {
        System.out.println("tASK:" + task_title);
        System.out.println("DB ID :" + mdl_db_id);
        System.out.println("Due Date:" + due_date);
        System.out.println("Android Token" + android_reg_token);
        String urlString = "https://monteapp.me/moodle/monteapi/authn/ToDoList/TodoList.php?InsertItem&mdl_db_id="+mdl_db_id+ "&due_date=" + due_date + "&task_title=" + Uri.encode(task_title)+ "&android_reg_token=" +android_reg_token;
        URL url = new URL(urlString);

        System.out.println(url.toString());
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.connect();
        System.out.println(connection.getResponseCode());

        /**
         * Now that the insert request has been made (successfully), we parse the JSON from HTTP response to get our TaskID (assigned on server)
         * and map it to the correct Task object in the Tasks arraylist
         */
        InputStream is = connection.getInputStream();
        StringBuffer sb = new StringBuffer();
        String response = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String inputLine = "";
        while ((inputLine = br.readLine()) != null) {
            sb.append(inputLine);
        }
        response = sb.toString(); //ta-da, it's a string now
        String step1 = "";

        try {
            Log.d("TaskIDFromResponse", response);
            String JsonString = "[" + response + "]";
            JSONArray initialArray = new JSONArray(JsonString); // - once to get rid of the extra data sent in the HTTP request (200 OK, etc) -

            // - and a second time to get the individual objects out of the step1 string (our desired JSON from server, without the extra junk
            JSONArray finalArray = null;

            String title = ""; //Store the title from JSON
            int id = 666; //Store the id from JSON
            Calendar dueDate = Calendar.getInstance(); //Store the due date from JSON

            for (int i = 0; i < initialArray.length(); i++) {
                step1 = initialArray.getJSONObject(i).getString("TaskID");
            }

            Log.d("TaskIDFromJSON", step1);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Integer.parseInt(step1);
    }

    public void deleteTask(String userID, int taskID) throws IOException{
        Log.d("userid", "" + userID);
        URL url = new URL("https://monteapp.me/moodle/monteapi/authn/ToDoList/TodoList.php?DeleteItem&mdl_db_id="+userID+"&ID="+taskID);
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.connect();
        System.out.println(connection.getResponseCode());
    }

    public ArrayList<Task> getTasksFromServer(String mdl_db_id) throws IOException {
        URL url = new URL("https://monteapp.me/moodle/monteapi/authn/ToDoList/TodoList.php?SendItemsToDevice&mdl_db_id=" + mdl_db_id);
        HttpURLConnection connection = null;
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("GET");
        connection.connect();

        //omg i can't believe we need this ugly-ass code to parse an HTTP response
        InputStream is = connection.getInputStream();
        StringBuffer sb = new StringBuffer();
        String tasks = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String inputLine = "";
        while ((inputLine = br.readLine()) != null) {
            sb.append(inputLine);
        }
        tasks = sb.toString(); //ta-da, it's a string now

        //Let's parse JSON into a usable arraylist!
        ArrayList<Task> tasksFromServer = new ArrayList<>(); //Construct a list to hold the soon-to-be extracted tasks

        //Ok, this is a bit of a process
        try {
            Log.d("TaskString", tasks);

            //We're gonna need two rounds of JSON parsing -
            String JsonString = "[" + tasks + "]";
            JSONArray initialArray = new JSONArray(JsonString); // - once to get rid of the extra data sent in the HTTP request (200 OK, etc) -
            String step1=""; //(we'll store that string here)

            // - and a second time to get the individual objects out of the step1 string (our desired JSON from server, without the extra junk
            JSONArray finalArray = null;

            String title= ""; //Store the title from JSON
            int id = 666; //Store the id from JSON
            Calendar dueDate = Calendar.getInstance(); //Store the due date from JSON

            for(int i = 0; i < initialArray.length(); i++)
            {
                step1 = initialArray.getJSONObject(i).getString("Tasks");
                String step2 =  step1; //had [ ] here before
                finalArray = new JSONArray(step2);

                //Log.d("haha", finalArray.getJSONObject(0).getString("task_title"), new Exception());

                for (int j = 0; j <finalArray.length();  j++)
                {
                    title = finalArray.getJSONObject(j).getString("task_title"); //Set title = the string returned by server
                    id =  Integer.parseInt(finalArray.getJSONObject(j).getString("ID")); //Parse the integer for id from the string returned by server
                    dueDate.setTimeInMillis((1000L * Long.parseLong(finalArray.getJSONObject(j).getString("due_date"))));//Set the calendar date to time in milliseconds from string in SECONDS from server (gotta multiply * 1000 to get millis)

                    Task temp = new Task(title, dueDate, id); //Construct a temp task for each iteration
                    tasksFromServer.add(temp); //Store each task in the list we created above                                  ^^^
                }

                Log.d("Tasks", tasksFromServer.toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tasksFromServer;
    }
}

