package hkucs.example.comp3330project;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataBaseCopy {
    private final String DB_NAME = "FoodDB.db";
    private Context context;
    public DataBaseCopy(Context context){
        this.context = context;
    }

    public String CopyDBFile() throws IOException{
        File dir = new File("data/data/"+context.getPackageName()+"/databases");
        if (!dir.exists() || !dir.isDirectory()){
            dir.mkdir();
        }
        File file = new File(dir,DB_NAME);
        InputStream inputStream = null;
        OutputStream outputStream = null;

        if (!file.exists()){
            try{
                file.createNewFile();
                inputStream = context.getClass().getClassLoader().getResourceAsStream(("assets/"+DB_NAME));
                outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len=inputStream.read(buffer))!=-1){
                    outputStream.write(buffer,0,len);
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
            finally {
                if (outputStream!=null){
                    outputStream.flush();
                    outputStream.close();
                }
                if(inputStream!=null){
                    inputStream.close();
                }
            }
        }
        return file.getPath();
    }
}
