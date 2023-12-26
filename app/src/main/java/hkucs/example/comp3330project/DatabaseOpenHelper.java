package hkucs.example.comp3330project;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatabaseOpenHelper extends SQLiteOpenHelper{
    private final static int DBVersion = 1;
    private final static String DBName = "FoodDB.db";
    private final static String TableName = "Food";
    private final static String CurrentDateName = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    public DatabaseOpenHelper(Context context) {
        super(context, DBName, null, DBVersion);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
