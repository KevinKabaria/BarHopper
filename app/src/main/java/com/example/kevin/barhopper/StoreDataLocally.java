package com.example.kevin.barhopper;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by kevin on 3/20/17.
 */

public class StoreDataLocally {

    public static void writeToFile(String filename, String data, Context ctx) {
        try (FileOutputStream fos =  ctx.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(data.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFromFile(String filename, Context ctx) {
        FileInputStream fis = null;
        try {
            fis = ctx.openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[10000];
        int val = 0;
        try {
            val = fis.read(buffer, 0, 10000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] convertString = new byte[val];

        for (int i=0; i<val; i++) {
            convertString[i] = buffer[i];
        }


        try {
            return new String(convertString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";

    }


}
