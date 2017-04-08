package alexandertech.mymonteuniversityhub.Classes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

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
public boolean logout(String SESSION){
    Boolean isDeleted = false;
    SQLiteDatabase myDB = this.getWritableDatabase();
    String statement = "DELETE FROM ActiveSessions WHERE SessionKey = '"+SESSION+"';";
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
     String urlParameters = "Task=clearSession&remoteDbId=" +ID;
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
     String urlParameters = "Task=newUser&FName=" + FName + "&LName="+ LName + "&remoteDBId="+remoteDbId+"&DeviceID=" + AndroidFCMID;
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


    public void insertTask(String task_title, String mdl_db_id, String due_date, String android_reg_token) throws IOException {
        System.out.println("tASK:" + task_title);
        System.out.println("DB ID :" + mdl_db_id);
        System.out.println("Due Date:" + due_date);
        System.out.println("Android Token" + android_reg_token);
        String urlParameters = "task_title=" + task_title + "&mdl_db_id=" + mdl_db_id + "&due_date=" + due_date + "&android_reg_token=" + android_reg_token;
        //URL url = new URL("https://monteapp.me/moodle/monteapi/authn/ToDoList/TodoList.php?InsertItem&" + urlParameters);
        URL url = new URL("https://monteapp.me/moodle/monteapi/authn/ToDoList/TodoList.php?InsertItem&mdl_db_id="+mdl_db_id+ "&due_date=5-6-94" + "&task_title=" +task_title+ "&android_reg_token=" +android_reg_token);
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
}

