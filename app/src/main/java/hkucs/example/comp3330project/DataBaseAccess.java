package hkucs.example.comp3330project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class DataBaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DataBaseAccess instance;

    Cursor c =null;
    private DataBaseAccess(Context context){

        this.openHelper=new DatabaseOpenHelper(context);
    }
    public static DataBaseAccess getInstance(Context context){
        if(instance==null){
            instance= new DataBaseAccess(context);

        }
        return instance;
    }

    public void open(){
        this.db=openHelper.getWritableDatabase();
    }

    public void close(){
        if(db!=null){
            this.db.close();
        }
    }


    public List<String> getDateRecord(String date){
        //String SQLTable ="CREATE TABLE IF NOT EXISTS '" + date + "' (Food TEXT);";
        String SQLTable ="CREATE TABLE IF NOT EXISTS DateRecord (Date Text PRIMARY KEY,Food TEXT);";
        db.execSQL(SQLTable);
        String SQLRecord = "INSERT OR IGNORE INTO DateRecord (Date, Food) VALUES('"+date+"','[]');";
        db.execSQL(SQLRecord);
        String query = "SELECT Food  FROM DateRecord WHERE Date = '"+date+"';";
        Cursor cursor = db.rawQuery(query, null);
        List<String> foodList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                // Retrieve the JSON data from the cursor
                String jsonData = cursor.getString(0);
                // Transform JSON data back to a list
                Gson gson = new Gson();
                List<String> foods = gson.fromJson(jsonData, new TypeToken<List<String>>(){}.getType());
                // Add the food names to the list
                foodList.addAll(foods);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return foodList;
    }

    public void addFood_to_dateRecord(String date, List<String> foodList){
        Gson gson = new Gson();
        String json = gson.toJson(foodList);
        ContentValues values = new ContentValues();
        values.put("Date",date);
        values.put("Food",json);
        //Log.d("DEBUGTEXT","add to database");
        db.replace("DateRecord",null,values);
    }

    public List<String> getFoodList(String date) {
        List<String> foodList = new ArrayList<>();
        // Define the SQL query to retrieve the JSON data
        String query = "SELECT Food  FROM DateRecord WHERE Date = '"+date+"';";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                // Retrieve the JSON data from the cursor
                String jsonData = cursor.getString(0);
                // Transform JSON data back to a list
                Gson gson = new Gson();
                List<String> foods = gson.fromJson(jsonData, new TypeToken<List<String>>(){}.getType());
                // Add the food names to the list
                foodList.addAll(foods);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return foodList;
    }



    public String getSum(String date, String col){
        //String SQL = "SELECT "+"'"+date+"'"+".rowid FROM '"+date+"' WHERE Food = " + food + " LIMIT 1";
        String Sql = "select sum("+col+") from \""+date+"\" LEFT JOIN Food ON \""+date+"\".Food =Food.name ";
        Log.d("DEBUGTEXT",Sql);
        c=db.rawQuery(Sql,null);
        c.moveToFirst();
        String msg = c.getString(0);
        return (msg);

    }
}
