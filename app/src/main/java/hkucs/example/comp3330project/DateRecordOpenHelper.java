package hkucs.example.comp3330project;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
public class DateRecordOpenHelper extends SQLiteOpenHelper{

    private final static int DBVersion = 1;
    private final static String DBName = "RecordDB.db";
    private final static String TableName = "Record";
    public DateRecordOpenHelper(Context context) {
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
