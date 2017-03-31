package alexandertech.mymonteuniversityhub.Classes;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
public boolean getUserLoginStatus(){
    boolean isSaved=false;
    SQLiteDatabase myDB = this.getWritableDatabase();
    String CheckInsert = "SELECT * FROM ActiveSessions";
    Cursor cursor = myDB.rawQuery(CheckInsert, null);
    if (cursor.moveToFirst()) {
        isSaved = true;
    } else {

        isSaved = false;
    }
    return isSaved;
}
    public String getID(){
        SQLiteDatabase myDB = this.getWritableDatabase();
        String dbID = "SELECT DatabaseID FROM ActiveSessions";
        Cursor cursor = myDB.rawQuery(dbID, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return " ";
    }
    public String getFName(){
        SQLiteDatabase myDB = this.getWritableDatabase();
        String FName = "SELECT FName FROM ActiveSessions";
        Cursor cursor = myDB.rawQuery(FName, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return " ";

    }
    public String getLName(){
        SQLiteDatabase myDB = this.getWritableDatabase();
        String LName = "SELECT LName FROM ActiveSessions";
        Cursor cursor = myDB.rawQuery(LName, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return " ";
    }
    public String getEmail(){
        SQLiteDatabase myDB = this.getWritableDatabase();
        String Email = "SELECT Email FROM ActiveSessions";
        Cursor cursor = myDB.rawQuery(Email, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return " ";
    }
    public String getSessionKey(){
        SQLiteDatabase myDB = this.getWritableDatabase();
        String SeshKey = "SELECT SessionKey FROM ActiveSessions";
        Cursor cursor = myDB.rawQuery(SeshKey, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        }
        return " ";
    }
public boolean logout(String SESSION){
    Boolean isDeleted = true;
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
    return isDeleted;
}




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

